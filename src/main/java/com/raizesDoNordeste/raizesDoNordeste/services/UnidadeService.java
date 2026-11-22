package com.raizesDoNordeste.raizesDoNordeste.services;

import com.raizesDoNordeste.raizesDoNordeste.entities.Unidade;
import com.raizesDoNordeste.raizesDoNordeste.repositories.UnidadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;

    public UnidadeService(UnidadeRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    public List<Unidade> listarTodos(){
        return unidadeRepository.findAll();
    }

    public Unidade salvar(Unidade unidade) {

//TRATAMENTO - Impede o cadastro de unidades que não possuam endereço

        if (unidade.getEndereco() == null || unidade.getEndereco().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O endereço da unidade não pode ser vazio.");
            }

        return unidadeRepository.save(unidade);
    }
    public Optional<Unidade> buscarPorId(Long id) {
        return unidadeRepository.findById(id);
    }
    public Optional<Unidade> atualizarUnidade(Long id, Unidade dadosAtualizados) {

        Optional<Unidade> unidadeEncontrada = unidadeRepository.findById(id);

        if (unidadeEncontrada.isPresent()) {

// TRATAMENTO - Impede a atualização com nome nulo ou vazio.
            if (dadosAtualizados.getNome() == null || dadosAtualizados.getNome().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O nome da unidade não pode ser vazio."
                );
            }

// TRATAMENTO - Impede a atualização com endereço nulo ou vazio.
            if (dadosAtualizados.getEndereco() == null || dadosAtualizados.getEndereco().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O endereço da unidade não pode ser vazio."
                );
            }

            Unidade unidade = unidadeEncontrada.get();

            unidade.setNome(dadosAtualizados.getNome());
            unidade.setEndereco(dadosAtualizados.getEndereco());

            unidadeRepository.save(unidade);

            return Optional.of(unidade);

        } else {
            return Optional.empty();
        }
    }


}
