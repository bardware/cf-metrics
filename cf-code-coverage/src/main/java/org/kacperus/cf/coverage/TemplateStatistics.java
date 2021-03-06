package org.kacperus.cf.coverage;

import java.io.File;
import java.util.BitSet;

import org.apache.log4j.Logger;

/**
 * Stores information about certain Cold Fusion template indicated by an absolute path.
 * Provides information about covered and visited lines containing CF markup.   
 */
public class TemplateStatistics {
	
	private static Logger log = Logger.getLogger(TemplateStatistics.class);
	
	private String templatePath;
	private BitSet coveredLines = new BitSet();
	private BitSet visitedLines = new BitSet();
	private long lastModifiedTimestamp = -1;
	
	public TemplateStatistics(String templatePath){
		if( templatePath == null ){
			throw new IllegalArgumentException("Template path cannot be null");
		}
		this.templatePath = templatePath;
		updateLastModifiedTimestamp();
	}
	
	public String getTemplatePath(){
		return templatePath;
	}
	
	public synchronized void markLineAsVisited(int lineNumber){
		if(coveredLines.get(lineNumber)){
			// line was covered so it may have been visited
			visitedLines.set(lineNumber);
		}else{
			// line was not covered prior to visit - strange situation
			log.debug("Line was visited but not covered: " + templatePath + ", " + lineNumber);
		}
	}
	
	public synchronized void markLineAsCovered(int lineNumber){
		coveredLines.set(lineNumber);
	}
	
	public synchronized boolean wasVisited(int lineNumber){
		return visitedLines.get(lineNumber);
	}

	public synchronized boolean wasCovered(int lineNumber){
		return coveredLines.get(lineNumber);
	}
	
	public synchronized int[] getVisitedLineNumbers(){
		return getSetBitIndexes(visitedLines);
	}

	public synchronized int[] getCoveredLineNumbers(){
		return getSetBitIndexes(coveredLines);
	}
	
	public synchronized int getVisitedLineCount(){
		return visitedLines.cardinality();
	}
	
	public synchronized int getCoveredLineCount(){
		return coveredLines.cardinality();
	}
	
	public synchronized long getVisitedPercentage(){
		int visitedCount = visitedLines.cardinality();
		int coveredCount = coveredLines.cardinality();
		if(coveredCount > 0){
			return Math.round( 100 * ((double)visitedCount/coveredCount) );
		}else{
			// if template has got no lines to cover (e.g. HTML only) then it can be considered as 100% covered
			return 100;
		}
	}
	
	public boolean isUpToDate() {
		File template = new File(templatePath);
		if( template.exists() ){
			return lastModifiedTimestamp == template.lastModified();
		}else{
			return false;
		}
	}

	public void updateLastModifiedTimestamp() {
		File template = new File(templatePath);
		if( template.exists() ){
			lastModifiedTimestamp = template.lastModified();
		}else{
			lastModifiedTimestamp = -1;
		}
	}

	/**
	 * Visited lines information is being cleared.
	 * Covered lines markers remain untouched.
	 */
	public synchronized void reset(){
		visitedLines.clear();
		updateLastModifiedTimestamp();
	}
	
	private int[] getSetBitIndexes(BitSet bs){
		int count = bs.cardinality();
		int index = 0;
		int[] arrIndexes = new int[count];
		if( count > 0 ){
			for(int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1)) {
				arrIndexes[index++] = i;
			}
		}
		return arrIndexes;
	}

}
