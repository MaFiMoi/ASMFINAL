package com.example.asmfinal.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.asmfinal.adapter.Expense;
import java.util.List;

public class SharedViewModel extends ViewModel {

    // LiveData cho danh sách chi tiêu. MutableLiveData cho phép cập nhật giá trị.
    private final MutableLiveData<List<Expense>> _expenses = new MutableLiveData<>();
    public LiveData<List<Expense>> getExpenses() {
        return _expenses;
    }

    // LiveData cho tổng chi tiêu
    private final MutableLiveData<Integer> _totalExpenses = new MutableLiveData<>();
    public LiveData<Integer> getTotalExpenses() {
        return _totalExpenses;
    }

    // LiveData cho tổng chi tiêu theo danh mục
    // private final MutableLiveData<Map<String, Float>> _categoryTotals = new MutableLiveData<>();
    // public LiveData<Map<String, Float>> getCategoryTotals() {
    //     return _categoryTotals;
    // }

    // Phương thức để cập nhật danh sách chi tiêu
    public void setExpenses(List<Expense> newExpenses) {
        _expenses.setValue(newExpenses);
    }

    // Phương thức để cập nhật tổng chi tiêu
    public void setTotalExpenses(int total) {
        _totalExpenses.setValue(total);
    }

    // Phương thức để cập nhật tổng chi tiêu theo danh mục
    // public void setCategoryTotals(Map<String, Float> totals) {
    //     _categoryTotals.setValue(totals);
    // }
}