package com.example.expensetracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenses = new ArrayList<>();
    private OnExpenseClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView amountText;
        private TextView categoryText;
        private TextView dateText;
        private TextView descriptionText;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.expenseAmount);
            categoryText = itemView.findViewById(R.id.expenseCategory);
            dateText = itemView.findViewById(R.id.expenseDate);
            descriptionText = itemView.findViewById(R.id.expenseDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onExpenseClick(expenses.get(position));
                }
            });
        }

        public void bind(Expense expense) {
            amountText.setText(String.format("$%.2f", expense.getAmount()));
            categoryText.setText(expense.getCategory());
            dateText.setText(dateFormat.format(expense.getDate()));
            descriptionText.setText(expense.getDescription());
        }
    }
} 