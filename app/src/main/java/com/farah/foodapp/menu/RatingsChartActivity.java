package com.farah.foodapp.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.farah.foodapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
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


public class RatingsChartActivity extends AppCompatActivity {//MPAndroidChart library

    private FirebaseFirestore db;
    private BarChart barChart;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_chart);

<<<<<<< Updated upstream
        // find views
        ImageButton backBtn = findViewById(R.id.btnBack);
=======
>>>>>>> Stashed changes

        // the back btt onclick
        ImageButton backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

// connect to xml
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        // get data from fb
        db = FirebaseFirestore.getInstance();
<<<<<<< Updated upstream

        //find admin id
=======
// this was sent we we opened the page using id now we want to put it in string
>>>>>>> Stashed changes
        String restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId != null) {
            loadRatingsData(restaurantId);
        }
        //back btn click listener
        backBtn.setOnClickListener(v -> onBackPressed());

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
                    //loop through each meal
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
<<<<<<< Updated upstream
                        Double ratingObj = doc.getDouble("rating");
                        String name = doc.getString("name");
                        // Get rating and meal name from Firestore
                        if (ratingObj != null && name != null) {
                            float rating = ratingObj.floatValue();
                            // Add entry to bar chart (x = index, y = rating)
                            barEntries.add(new BarEntry(index, rating));
                            pieEntries.add(new PieEntry(rating, formatLabel(name)));
                            labels.add(formatLabel(name));
                            index++; // move to next x-axis position
=======
                        Double ratingObj = doc.getDouble("rating");// the rate of each meal
                        String name = doc.getString("name");// name of the meal

                        if (ratingObj != null && name != null) {
                            float rating = ratingObj.floatValue();
                            barEntries.add(new BarEntry(index, rating));//ex: bar index 0 has 4.8 value
                            pieEntries.add(new PieEntry(rating, formatLabel(name)));
                            labels.add(formatLabel(name));// names under bars are distributed in a list
                            index++;
>>>>>>> Stashed changes
                        }
                    }

                    // define shades of red to use when constructing bar and pie charts
                    int[] reds = {
                            Color.parseColor("#8B0000"),
                            Color.parseColor("#A40000"),
                            Color.parseColor("#C21807"),
                            Color.parseColor("#E53935")
                    };

<<<<<<< Updated upstream
                    // Create dataset for bar chart
                    BarDataSet dataSet = new BarDataSet(barEntries, "Meal Ratings");
=======
                    // BAR CHART
                    BarDataSet dataSet = new BarDataSet(barEntries, "Meal Ratings");//par2: data set name used for drawing

                    //use to choose and repeate colors
>>>>>>> Stashed changes
                    List<Integer> colorList = new ArrayList<>();
                    for (int i = 0; i < labels.size(); i++) {// count on meal names
                        colorList.add(reds[i % reds.length]);
                    }
<<<<<<< Updated upstream
                    //Assign colors to bars
                    dataSet.setColors(colorList);
                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setValueTextSize(13f);

                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.4f);// width of each bar
                    barChart.setData(barData);

                    // Configure X-axis (meal names)
                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));// show meal names
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);// one label per bar
                    xAxis.setDrawGridLines(false);
                    xAxis.setTextSize(11f);
                    xAxis.setTextColor(Color.BLACK);
                    xAxis.setLabelRotationAngle(0f);
                    xAxis.setLabelCount(labels.size());
                    xAxis.setYOffset(40f);
                    xAxis.setAvoidFirstLastClipping(true);
                    // Add extra space for labels
                    barChart.setExtraBottomOffset(110f);

                    //Description of bar chart
                    Description barDesc = new Description();
                    barDesc.setText("Meal Ratings Overview");
                    barChart.setDescription(barDesc);

                    // Configure Y-axis (rating scale)
                    barChart.getAxisLeft().setAxisMinimum(0f);
                    barChart.getAxisLeft().setAxisMaximum(5f);//ratings from 0 to 5
                    barChart.getAxisRight().setEnabled(false); // disable right axis
                    barChart.getLegend().setEnabled(false);
                    barChart.animateY(1200);
                    barChart.invalidate();

                    // Create dataset for pie chart
=======
                    dataSet.setColors(colorList);// the list of colors is taken
                    dataSet.setValueTextColor(Color.BLACK);// text above bar color
                    dataSet.setValueTextSize(13f);//size of number above bar

                    //BarEntry single bar
                    //dataset= coloums with its colors and values
                    //bardata that allows us to draw

                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.4f);// width of bar
                    barChart.setData(barData);// draw

                    XAxis xAxis = barChart.getXAxis();//get x aixs
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));// give values from labels
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);// x aixs under bars
                    xAxis.setGranularity(1f);// no bars at 0.5 only 1 then 2
                    xAxis.setDrawGridLines(false);//no gridlines
                    xAxis.setTextSize(11f);
                    xAxis.setTextColor(Color.BLACK);
                    xAxis.setLabelRotationAngle(0f);// horizantal view of the word no curve
                    xAxis.setLabelCount(labels.size());// to avoid auto skip we must know how many names i have
                    xAxis.setYOffset(40f);// extra space between lines
                    xAxis.setAvoidFirstLastClipping(true);// the last and first name wont be cut if long
                    barChart.setExtraBottomOffset(110f);// give space for long

                    barChart.getAxisLeft().setAxisMinimum(0f);// min of y axis is 0
                    barChart.getAxisLeft().setAxisMaximum(5f);// max of y is 5
                    barChart.getAxisRight().setEnabled(false);// i dont want right axis
                    barChart.getLegend().setEnabled(false);// chart key no need because all bars represent ratings
                    barChart.animateY(1200);// bars rise from bottom to above in 1.2 sec
                    barChart.invalidate();// draw now

                    // PIE CHART
                    //pieEntries rate , name
                    //pie data set is the collection of entries and are trated as one
                    // pie data is the final result of drawing
>>>>>>> Stashed changes
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                    pieDataSet.setColors(colorList);
                    pieDataSet.setValueTextSize(12f);
                    pieDataSet.setValueTextColor(Color.WHITE);
<<<<<<< Updated upstream

                    // Wrap dataset inside PieData
                    PieData pieData = new PieData(pieDataSet);

                    pieChart.setData(pieData);
                    pieChart.setUsePercentValues(false);// show raw values, not percentages
                    pieChart.getDescription().setEnabled(false);
                    pieChart.setCenterText("Ratings %");
=======
                    PieData pieData = new PieData(pieDataSet);// final result of drawing

                    pieChart.setData(pieData);// this is the data to draw
                    pieChart.setUsePercentValues(false);//dont want percentage
                    pieChart.getDescription().setEnabled(false);// without decr
                    pieChart.setCenterText("Ratings %");// the center title
>>>>>>> Stashed changes
                    pieChart.setCenterTextSize(14f);
                    pieChart.setHoleRadius(35f);// determine center size
                    pieChart.animateY(1500);
<<<<<<< Updated upstream
                    pieChart.invalidate();// refresh chart
=======
                    pieChart.invalidate();// draw
>>>>>>> Stashed changes
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
