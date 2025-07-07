package br.com.smartvalidity.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralDTO;

@Service
public class ExcelService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public byte[] gerarExcelMural(List<MuralDTO.Listagem> itens, String titulo) throws SmartValidityException {
        logger.info("Iniciando geracao de relatorio Excel: {}", titulo);
        logger.debug("Quantidade de itens para o relatorio: {}", itens.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mural");
            
            // Estilos
            CellStyle headerStyle = criarEstiloCabecalho(workbook);
            CellStyle dateStyle = criarEstiloData(workbook);
            
            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            
            // Cabeçalhos
            Row headerRow = sheet.createRow(2);
            String[] headers = {
                "Produto", "Marca", "Categoria", "Corredor", "Fornecedor",
                "Data Fabricação", "Data Recebimento", "Data Vencimento",
                "Lote", "Status", "Inspecionado", "Motivo Inspeção", "Usuário Inspeção"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 256 * 20); // 20 caracteres de largura
            }
            
            // Dados
            int rowNum = 3;
            int processados = 0;
            for (MuralDTO.Listagem item : itens) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(item.getProduto() != null ? item.getProduto().getDescricao() : "");
                row.createCell(1).setCellValue(item.getProduto() != null ? item.getProduto().getMarca() : "");
                row.createCell(2).setCellValue(item.getCategoria());
                row.createCell(3).setCellValue(item.getCorredor());
                row.createCell(4).setCellValue(item.getFornecedor());
                
                // Datas
                Cell fabCell = row.createCell(5);
                Cell recCell = row.createCell(6);
                Cell venCell = row.createCell(7);
                
                if (item.getDataFabricacao() != null) {
                    fabCell.setCellValue(item.getDataFabricacao().format(DATE_FORMATTER));
                }
                if (item.getDataRecebimento() != null) {
                    recCell.setCellValue(item.getDataRecebimento().format(DATE_FORMATTER));
                }
                if (item.getDataValidade() != null) {
                    venCell.setCellValue(item.getDataValidade().format(DATE_FORMATTER));
                }
                
                fabCell.setCellStyle(dateStyle);
                recCell.setCellStyle(dateStyle);
                venCell.setCellStyle(dateStyle);
                
                row.createCell(8).setCellValue(item.getLote());
                row.createCell(9).setCellValue(item.getStatus());
                row.createCell(10).setCellValue(item.getInspecionado() != null && item.getInspecionado() ? "Sim" : "Não");
                row.createCell(11).setCellValue(item.getMotivoInspecao() != null ? item.getMotivoInspecao() : "");
                row.createCell(12).setCellValue(item.getUsuarioInspecao() != null ? item.getUsuarioInspecao() : "");
                
                processados++;
                if (processados % 100 == 0) {
                    logger.debug("Processados {} de {} itens", processados, itens.size());
                }
            }
            
            // Auto-size para todas as colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Converter para bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] resultado = outputStream.toByteArray();
            
            logger.info("Relatorio Excel gerado com sucesso: {} itens processados, {} bytes", itens.size(), resultado.length);
            return resultado;
            
        } catch (Exception e) {
            logger.error("Erro ao gerar arquivo Excel: {}", e.getMessage(), e);
            throw new SmartValidityException("Erro ao gerar arquivo Excel: " + e.getMessage());
        }
    }
    
    private CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle criarEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }
}