package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.repository.CategoriaRepository;
import br.com.smartvalidity.model.repository.CorredorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CorredorService corredorService;

    @Autowired
    private CorredorRepository corredorRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Integer id) throws SmartValidityException {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Categoria não encontrada com o ID: " + id));
    }

    public Categoria salvar(Categoria categoria) throws SmartValidityException {
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Integer id, Categoria categoriaAtualizada) throws SmartValidityException {
        Categoria categoria = buscarPorId(id);

        categoria.setNome(categoriaAtualizada.getNome());
        categoria.setCorredor(categoriaAtualizada.getCorredor());

        return categoriaRepository.save(categoria);
    }

    public void excluir(Integer id) throws SmartValidityException {
        Categoria categoria = buscarPorId(id);
        categoriaRepository.delete(categoria);
    }
}
