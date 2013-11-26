/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old;

import weka.attributeSelection.*;
import weka.core.*;
import weka.core.converters.ConverterUtils.*;
import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;
import weka.filters.*;

import java.util.*;

/**
 * performs attribute selection using CfsSubsetEval and GreedyStepwise
 * (backwards) and trains J48 with that. Needs 3.5.5 or higher to compile.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CFS {

  /**
   * uses the meta-classifier
   */
  protected static void useClassifier(Instances data) throws Exception {
    System.out.println("\n1. Meta-classfier");
    AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
    ChiSquaredAttributeEval eval = new ChiSquaredAttributeEval();
    Ranker search = new Ranker();
    search.setThreshold(-1.7976931348623157E308);
    search.setNumToSelect(1000);
    J48 base = new J48();
    classifier.setClassifier(base);
    classifier.setEvaluator(eval);
    classifier.setSearch(search);
    Evaluation evaluation = new Evaluation(data);
    evaluation.crossValidateModel(classifier, data, 10, new Random(1));
    System.out.println(evaluation.toSummaryString());
  }

  /**
   * uses the filter
   */
  protected static void useFilter(Instances data) throws Exception {
    System.out.println("\n2. Filter");
    weka.filters.supervised.attribute.AttributeSelection filter = new weka.filters.supervised.attribute.AttributeSelection();
    ChiSquaredAttributeEval eval = new ChiSquaredAttributeEval();
    
    Ranker search = new Ranker();
    search.setThreshold(-1.7976931348623157E308);
    search.setNumToSelect(1000);
    filter.setEvaluator(eval);
   
    filter.setSearch(search);
    filter.setInputFormat(data);
    Instances newData = Filter.useFilter(data, filter);
    System.out.println(newData);
  }

  /**
   * uses the low level approach
     * @param data
   */
  protected static void useLowLevel(Instances data) throws Exception {
    System.out.println("\n3. Low-level");
    AttributeSelection attsel = new AttributeSelection();
    ChiSquaredAttributeEval eval = new ChiSquaredAttributeEval();
    Ranker search = new Ranker();
    search.setThreshold(-1.7976931348623157E308);
    search.setNumToSelect(1000);
    attsel.setEvaluator(eval);
    attsel.setSearch(search);
    attsel.setFolds(10);
    attsel.setXval(true);
    attsel.SelectAttributes(data);
//    System.out.println(data.toSummaryString());
//    attsel.selectAttributesCVSplit(data);
//    attsel.SelectAttributes(data);
    
    System.out.println(attsel.CrossValidateAttributes());
//    attsel.SelectAttributes(data);
//    attsel.selectAttributesCVSplit(data);
    Instances newData=attsel.reduceDimensionality(data);
    
    int[] indices = attsel.selectedAttributes();
    System.out.println(newData);
    System.out.println("selected attribute indices (starting with 0):\n" + Utils.arrayToString(indices));
  }

  /**
   * takes a dataset as first argument
   *
   * @param args        the commandline arguments
   * @throws Exception  if something goes wrong
   */
  public static void main(String[] args) throws Exception {
    // load data
    System.out.println("\n0. Loading data");
    DataSource source = new DataSource("D:\\ALL\\imdb_grid_size=1000_MIN=50_genres=5.arff");
    Instances data = source.getDataSet();
    data.setClass(data.attribute("Horror"));
    
    if (data.classIndex() == -1)
      data.setClassIndex(data.numAttributes() - 1);

//    // 1. meta-classifier
//    useClassifier(data);
//
//    // 2. filter
//    useFilter(data);

    // 3. low-level
    useLowLevel(data);
  }
}
