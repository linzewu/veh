package com.xs.veh.util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

import com.xs.veh.entity.CheckPhoto;

public class JFreeChartUtil {
	public static void main(String[] args) {
	    DefaultCategoryDataset ds = new DefaultCategoryDataset();
		ds.addValue(10, "ibm", "2018-05-21");
        ds.addValue(20, "ibm", "2018-05-22");
        ds.addValue(32, "ibm", "2018-05-23");
        ds.addValue(25, "ibm", "2018-05-24");
        ds.addValue(0, "ibm", "2018-05-25");
        ds.addValue(4, "ibm", "2018-05-26");
        ds.addValue(32, "ibm", "2018-05-27");
        ds.addValue(0, "ibm", "2018-05-28");
        ds.addValue(358, "ibm", "2018-05-29");
        ds.addValue(4, "ibm", "2018-05-30");
        
    	ds.addValue(50, "nab", "2018-05-21");
        ds.addValue(10, "nab", "2018-05-22");
        ds.addValue(21, "nab", "2018-05-23");
        ds.addValue(25, "nab", "2018-05-24");
        ds.addValue(0, "nab", "2018-05-25");
        ds.addValue(4, "nab", "2018-05-26");
        ds.addValue(99, "nab", "2018-05-27");
        ds.addValue(0, "nab", "2018-05-28");
        ds.addValue(197, "nab", "2018-05-29");
        ds.addValue(200, "nab", "2018-05-30");
        
        String filePath = "d:/lgg.jpg";
        createLineChart(ds,filePath);
   }
    
   public static InputStream createLineChart(DefaultCategoryDataset ds, String filePath) {
	   
        try {
            JFreeChart chart = ChartFactory.createLineChart("", "", "", ds, PlotOrientation.VERTICAL,false, true, true);
            chart.setBackgroundPaint(Color.WHITE);
            Font font = new Font("宋体", Font.BOLD, 12);
            chart.getTitle().setFont(font);
            chart.setBackgroundPaint(Color.WHITE);
            // 配置字体（解决中文乱码的通用方法）
            Font xfont = new Font("仿宋", Font.BOLD, 12); // X轴
            Font yfont = new Font("宋体", Font.BOLD, 12); // Y轴
            Font titleFont = new Font("宋体", Font.BOLD, 12); // 图片标题
            CategoryPlot categoryPlot = chart.getCategoryPlot();
            categoryPlot.getDomainAxis().setLabelFont(xfont);
            
            categoryPlot.getRangeAxis().setLabelFont(yfont);
            chart.getTitle().setFont(titleFont);
            categoryPlot.setBackgroundPaint(Color.WHITE);
            // x轴 // 分类轴网格是否可见
            categoryPlot.setDomainGridlinesVisible(true);
            // y轴 //数据轴网格是否可见
            categoryPlot.setRangeGridlinesVisible(true);
            // 设置网格竖线颜色
            categoryPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            // 设置网格横线颜色
            categoryPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            // 没有数据时显示的文字说明
            categoryPlot.setNoDataMessage("没有数据显示");
            // 设置曲线图与xy轴的距离
            categoryPlot.setAxisOffset(new RectangleInsets(0d, 0d, 0d, 0d));
            // 设置面板字体
            Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);
            // 取得Y轴
            NumberAxis rangeAxis = (NumberAxis) categoryPlot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setAutoRangeIncludesZero(true);
            rangeAxis.setUpperMargin(0.010);
            rangeAxis.setLabelAngle(Math.PI / 2.0);
            // 取得X轴
            CategoryAxis categoryAxis = (CategoryAxis) categoryPlot.getDomainAxis();
            // 设置X轴坐标上的文字
            categoryAxis.setTickLabelFont(labelFont);
            // 设置X轴的标题文字
            categoryAxis.setLabelFont(labelFont);
            // 横轴上的 Lable 45度倾斜
            categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
            
            categoryAxis.setVisible(false);
            
            // 设置距离图片左端距离
            categoryAxis.setLowerMargin(0.0);
            // 设置距离图片右端距离
            categoryAxis.setUpperMargin(0.0);
            // 获得renderer 注意这里是下嗍造型到lineandshaperenderer！！
            LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryPlot.getRenderer();
            // 是否显示折点
            lineandshaperenderer.setBaseShapesVisible(false);
            // 是否显示折线
            lineandshaperenderer.setBaseLinesVisible(true);
            // series 点（即数据点）间有连线可见 显示折点数据
            lineandshaperenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            lineandshaperenderer.setBaseItemLabelsVisible(false);
            
            File file=new File(filePath);
            
            ChartUtilities.saveChartAsJPEG(file, chart, 600, 300);
            
            return new FileInputStream(file);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
    }

}
