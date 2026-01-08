package com.farah.foodapp.admin.admin_profile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.farah.foodapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormatSymbols;
import java.util.*;

public class StatsTabActivity extends Fragment {

    //UI elements
    private TextView tvRevenue, tvCustomers, tvRating, tvPrediction;
    private BarChart barChart;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.stats_tab_admin, container, false);

        // Initialize TextViews and set their labels
        tvRevenue = view.findViewById(R.id.card_total_revenue).findViewById(R.id.tv_value);
        ((TextView) view.findViewById(R.id.card_total_revenue).findViewById(R.id.tv_label))
                .setText("Total Revenue");

        tvCustomers = view.findViewById(R.id.card_total_customers).findViewById(R.id.tv_value);
        ((TextView) view.findViewById(R.id.card_total_customers).findViewById(R.id.tv_label))
                .setText("Total Customers");

        tvRating = view.findViewById(R.id.card_avg_rating).findViewById(R.id.tv_value);
        ((TextView) view.findViewById(R.id.card_avg_rating).findViewById(R.id.tv_label))
                .setText("Avg Rating");

        // Initialize charts
        barChart = view.findViewById(R.id.barChartOrders);
        pieChart = view.findViewById(R.id.pieChartCategories);
        tvPrediction = view.findViewById(R.id.tv_prediction);

        loadStats();
        return view;
    }


    //Fetch current restaurant's data and trigger charts loading.
    private void loadStats() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("restaurants").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String restaurantName = doc.getString("name");
                        if (restaurantName != null) loadCharts(db, restaurantName);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadCharts(FirebaseFirestore db, String restaurantName) {
        db.collection("orders").get()
                .addOnSuccessListener(query -> {
                    double totalRevenue = 0;
                    double totalRating = 0;
                    int ratedOrders = 0;
                    Set<String> customers = new HashSet<>();

                    Map<Integer, Float> monthlyRevenue = new HashMap<>();
                    Map<String, Integer> statusCount = new HashMap<>();

                    // Iterate orders and calculate metrics
                    for (QueryDocumentSnapshot doc : query) {
                        String docRestaurant = doc.getString("restaurantName");
                        if (docRestaurant == null || !docRestaurant.equalsIgnoreCase(restaurantName)) continue;

                        Double total = doc.getDouble("total");
                        if (total != null) totalRevenue += total;

                        String userId = doc.getString("userId");
                        if (userId != null) customers.add(userId);

                        Double rating = doc.getDouble("rating");
                        if (rating != null && rating > 0) {
                            totalRating += rating;
                            ratedOrders++;
                        }

                        // Extract month from order date
                        Object createdAtObj = doc.get("createdAt");
                        Date date = null;
                        if (createdAtObj instanceof com.google.firebase.Timestamp) {
                            date = ((com.google.firebase.Timestamp) createdAtObj).toDate();
                        } else if (createdAtObj instanceof Long) {
                            date = new Date((Long) createdAtObj);
                        }

                        if (date != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            int month = cal.get(Calendar.MONTH);
                            float orderTotal = total != null ? total.floatValue() : 0f;
                            monthlyRevenue.put(month,
                                    monthlyRevenue.getOrDefault(month, 0f) + orderTotal);
                        }

                        String status = doc.getString("status");
                        if (status == null || status.isEmpty()) status = "Unknown";
                        statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
                    }

                    // Update TextViews
                    tvRevenue.setText(String.format("%.2f JOD", totalRevenue));
                    tvCustomers.setText(String.valueOf(customers.size()));
                    tvRating.setText(ratedOrders > 0
                            ? String.format("%.1f ★", totalRating / ratedOrders)
                            : "N/A");

                    // Setup charts
                    setupBarChart(monthlyRevenue);
                    setupPieChart(statusCount);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    //Predict next month's revenue using simple linear trend => y = slope * month + intercept
    private float predictNextMonthWithTrend(Map<Integer, Float> monthlyRevenue) {
        List<Integer> months = new ArrayList<>();
        List<Float> revenues = new ArrayList<>();

        // Collect data
        for (int i = 0; i < 12; i++) {
            if (monthlyRevenue.containsKey(i)) {
                months.add(i);
                revenues.add(monthlyRevenue.get(i));
            }
        }

        if (months.size() < 2) return 0f;

        float meanX = 0, meanY = 0;
        for (int i = 0; i < months.size(); i++) {
            meanX += months.get(i);
            meanY += revenues.get(i);
        }
        // Mean of months
        meanX /= months.size();
        // Mean of revenues
        meanY /= months.size();

        float numerator = 0, denominator = 0;
        for (int i = 0; i < months.size(); i++) {
            numerator += (months.get(i) - meanX) * (revenues.get(i) - meanY);
            denominator += Math.pow(months.get(i) - meanX, 2);
        }

        float slope = numerator / denominator;
        float intercept = meanY - slope * meanX;

        int nextMonth = Collections.max(months) + 1;
        return intercept + slope * nextMonth;
    }

    //Setup monthly revenue bar chart including predicted next month
    private void setupBarChart(Map<Integer, Float> monthlyRevenue) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyRevenue.getOrDefault(i, 0f)));
        }

        float predictedValue = predictNextMonthWithTrend(monthlyRevenue);

        float lastMonthRevenue = 0f;
        if (!monthlyRevenue.isEmpty()) {
            int lastMonth = Collections.max(monthlyRevenue.keySet());
            lastMonthRevenue = monthlyRevenue.get(lastMonth);
        }

        float percentageChange = 0f;
        String arrow = "";
        if (lastMonthRevenue > 0 && predictedValue > 0) {
            percentageChange = ((predictedValue - lastMonthRevenue) / lastMonthRevenue) * 100;
            arrow = predictedValue > lastMonthRevenue ? "↑" : "↓";
        }

        //Display prediction
        if (tvPrediction != null) {
            if (predictedValue > 0) {
                String trendText = String.format(Locale.getDefault(),
                        "Next Month (Predicted): %.2f JOD %s %.1f%%",
                        predictedValue, arrow, Math.abs(percentageChange));
                tvPrediction.setText(trendText);

                if (predictedValue > lastMonthRevenue) {
                    tvPrediction.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
                } else if (predictedValue < lastMonthRevenue) {
                    tvPrediction.setTextColor(ContextCompat.getColor(requireContext(), R.color.red2));
                } else {
                    tvPrediction.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_app));
                }
            } else {
                tvPrediction.setText("Prediction unavailable");
                tvPrediction.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            }
        }

        if (predictedValue > 0) {
            entries.add(new BarEntry(12, predictedValue));
        }

        // Configure bar chart dataset
        BarDataSet dataSet = new BarDataSet(entries, "Monthly Revenue (JOD)");
        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.orange_app));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        if (entries.size() > 12) {
            dataSet.addColor(ContextCompat.getColor(requireContext(), R.color.gray_light));
        }

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);
        barChart.setData(data);

        // Configure Y axis
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f JOD", value);
            }
        });
        yAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground));
        yAxis.setTextSize(12f);
        yAxis.setGranularity(10f);
        barChart.getAxisRight().setEnabled(false);

        // Configure X axis
        String[] months = new DateFormatSymbols().getShortMonths();
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index < 12) return months[index];
                else return "Next";
            }
        });
        barChart.getXAxis().setGranularity(1f);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    //Setup pie chart to show order status distribution
    private void setupPieChart(Map<String, Integer> statusCount) {
        if (statusCount.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No status data available");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Integer> e : statusCount.entrySet()) {
            String status = e.getKey();
            entries.add(new PieEntry(e.getValue(), status));

            int color;
            if (status.equalsIgnoreCase("Completed")) {
                color = ContextCompat.getColor(requireContext(), R.color.green);
            } else if (status.equalsIgnoreCase("Pending")) {
                color = ContextCompat.getColor(requireContext(), R.color.orange_app);
            } else if (status.equalsIgnoreCase("Cancelled")) {
                color = ContextCompat.getColor(requireContext(), R.color.red2);
            } else if (status.equalsIgnoreCase("Preparing")) {
                color = ContextCompat.getColor(requireContext(), R.color.blue);
            } else {
                color = ContextCompat.getColor(requireContext(), R.color.gray);
            }
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Order Status");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}
