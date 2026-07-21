package com.example.myProject.controller;


import com.example.myProject.entity.BookEntry;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookEntryController {

    private Map<Long, BookEntry> bookEntries = new HashMap<>();
    @GetMapping
    public List<BookEntry> getAll(){
        return new ArrayList<>(bookEntries.values());
    }

    @PostMapping
    public boolean createEntry(@RequestBody BookEntry entry){
        bookEntries.put(entry.getId(), entry);
        return true;

    }

    @GetMapping("ID/{id}")
    public BookEntry getBookById(@PathVariable Long id){

        return bookEntries.get(id);
    }
    @DeleteMapping("ID/{id}")
    public BookEntry deleteBookById(@PathVariable Long id){

        return bookEntries.remove(id);
    }
}
