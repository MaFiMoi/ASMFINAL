package com.example.asmfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.asmfinal.adapter.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE strftime('%m-%Y', date) = :monthYear ORDER BY date DESC")
    List<Expense> getExpensesByMonth(String monthYear);

    @Query("SELECT SUM(amount) FROM expenses WHERE strftime('%m-%Y', date) = :monthYear")
    Integer getTotalSpentByMonth(String monthYear);
}
