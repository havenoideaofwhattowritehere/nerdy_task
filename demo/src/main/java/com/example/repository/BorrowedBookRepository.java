package com.example.repository;

import com.example.entity.BorrowedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {
    List<BorrowedBook> findByMemberName(String memberName);

    long countByMemberId(Long memberId);

    @Query("SELECT DISTINCT b.book.title FROM BorrowedBook b")
    List<String> findDistinctBookNames();

    @Query("SELECT new map(b.book.title as title, COUNT(b) as count) " +
            "FROM BorrowedBook b GROUP BY b.book.title")
    List<Map<String, Object>> findBooksWithBorrowCount();
}
