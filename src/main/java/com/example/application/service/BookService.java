package com.example.application.service;


import com.example.application.models.VolumesResponse;
import com.example.application.repository.BookRepository;
import org.springframework.stereotype.Service;


@Service
public class BookService {

    public static final int MAX_RESULTS = 20;
    private BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

   //READ paged
    public void getBooks(AsyncRestCallback<VolumesResponse> callback, String search, int maxResults,
                         int startIndex) {

        System.out.println("fetching books -> " + startIndex + " to "
                + (startIndex + MAX_RESULTS - 1));

        bookRepository.getBooks(callback, search, maxResults, startIndex);


    }

}