package com.farah.foodapp.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.farah.foodapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RatingsChartActivity extends AppCompatActivity { // MPAndroidChart library

    private FirebaseFirestore db;
    private BarChart barChart;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_chart);

        // find views
        ImageButton backBtn = findViewById(R.id.btnBack);

        // the back btn onclick
        backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // connect to xml
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

        // get data from fb
        db = FirebaseFirestore.getInstance();

        // this was sent when we opened the page using id now we want to put it in string
        String restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId != null) {
            loadRatingsData(restaurantId);
        }
    }

    private void loadRatingsData(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .collection("menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // charts need entries not docs
                    ArrayList<BarEntry> barEntries = new ArrayList<>();
                    ArrayList<PieEntry> pieEntries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();

                    int index = 0;

                    // loop through each meal
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Double ratingObj = doc.getDouble("rating"); // the rate of each meal
                        String name = doc.getString("name");        // name of the meal

                        if (ratingObj != null && name != null) {
                            float rating = ratingObj.floatValue();

                            // ex: bar index 0 has 4.8 value
                            barEntries.add(new BarEntry(index, rating));

                            pieEntries.add(new PieEntry(rating, formatLabel(name)));

                            // names under bars are distributed in a list
                            labels.add(formatLabel(name));

                            index++;
                        }
                    }

                    // define shades of red to use when constructing bar and pie charts
                    int[] reds = {
                            Color.parseColor("#8B0000"),
                            Color.parseColor("#A40000"),
                            Color.parseColor("#C21807"),
                            Color.parseColor("#E53935")
                    };

                    // BAR CHART
                    BarDataSet dataSet = new BarDataSet(barEntries, "Meal Ratings"); // par2: data set name used for drawing

                    // use to choose and repeat colors
                    List<Integer> colorList = new ArrayList<>();
                    for (int i = 0; i < labels.size(); i++) { // count on meal names
                        colorList.add(reds[i % reds.length]);
                    }

                    dataSet.setColors(colorList);           // the list of colors is taken
                    dataSet.setValueTextColor(Color.BLACK); // text above bar color
                    dataSet.setValueTextSize(13f);          // size of number above bar

                    // BarEntry = single bar
                    // DataSet = columns with its colors and values
                    // BarData = allows us to draw
                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.4f); // width of bar
                    barChart.setData(barData); // draw

                    // X Axis settings
                    XAxis xAxis = barChart.getXAxis(); // get x axis
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // give values from labels
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x axis under bars
                    xAxis.setGranularity(1f); // no bars at 0.5 only 1 then 2
                    xAxis.setDrawGridLines(false); // no gridlines
                    xAxis.setTextSize(11f);
                    xAxis.setTextColor(Color.BLACK);
                    xAxis.setLabelRotationAngle(0f); // horizontal view
                    xAxis.setLabelCount(labels.size()); // avoid auto skip
                    xAxis.setYOffset(40f); // extra space
                    xAxis.setAvoidFirstLastClipping(true); // avoid cutting labels

                    barChart.setExtraBottomOffset(110f); // space for long names
                    barChart.getAxisLeft().setAxisMinimum(0f); // min of y axis
                    barChart.getAxisLeft().setAxisMaximum(5f); // max of y axis
                    barChart.getAxisRight().setEnabled(false); // disable right axis
                    barChart.getLegend().setEnabled(false); // no legend
                    barChart.animateY(1200); // animation
                    barChart.invalidate(); // draw now

                    // PIE CHART
                    // pieEntries = rate , name
                    // PieDataSet = collection of entries
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                    pieDataSet.setColors(colorList);
                    pieDataSet.setValueTextSize(12f);
                    pieDataSet.setValueTextColor(Color.WHITE);

                    // PieData = final result of drawing
                    PieData pieData = new PieData(pieDataSet);
                    pieChart.setData(pieData);
                    pieChart.setUsePercentValues(false); // don't want percentage
                    pieChart.getDescription().setEnabled(false); // no description
                    pieChart.setCenterText("Ratings %"); // center title
                    pieChart.setCenterTextSize(14f);
                    pieChart.setHoleRadius(35f); // center size
                    pieChart.animateY(1500);
                    pieChart.invalidate(); // draw
                });
    }

    private String formatLabel(String label) {
        String[] words = label.split(" ");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            formatted.append(words[i]);

            if ((i + 1) % 2 == 0 && i != words.length - 1) {
                formatted.append("\n");
            } else {
                formatted.append(" ");
            }
        }
        return formatted.toString().trim();
    }
}
