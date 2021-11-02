package io.dekorate.example.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/fruits")
public class FruitController {

    private final FruitRepository repository;

    public FruitController(FruitRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}")
    public Fruit get(@PathVariable("id") Integer id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    @GetMapping
    public List<Fruit> getAll() {
        Spliterator<Fruit> fruits = repository.findAll()
                .spliterator();

        return StreamSupport
                .stream(fruits, false)
                .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Fruit post(@RequestBody(required = false) Fruit fruit) {
        return repository.save(fruit);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    public Fruit put(@PathVariable("id") Integer id, @RequestBody(required = false) Fruit fruit) {
        fruit.setId(id);
        return repository.save(fruit);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        repository.deleteById(id);
    }

}
