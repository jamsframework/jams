package optas.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author Nathan Lighthart
 */
public class RankingTable {
	private RankedSample[] table;
	
	public RankingTable(List<Sample> samples) {
		table = new RankedSample[samples.size()];
		int i = 0;
		for(Sample sample : samples) {
			table[i] = new RankedSample(sample);
			i++;
		}
	}
	
	public List<Sample> getTop(int n) {
		List<Sample> topList = new ArrayList<Sample>();
		for(int i = 0; i < n && i < table.length; i++) {
			topList.add(table[i].sample);
		}
		return topList;
	}
	
	
	public void computeRankings() {
		int m = table[0].sample.F().length;
		for(int efficiency = 0; efficiency < m; efficiency++) {
			Arrays.sort(table, new SampleColumnComparator(efficiency));
			accumulateRank();
		}
		Arrays.sort(table, new RankComparator());
	}
	
	private void accumulateRank() {
		int rank = 1;
		for(RankedSample rs : table) {
			rs.rank += rank;
			rank++;
		}
	}
	
	@Override
	public String toString() {
		String s = "";
		for(RankedSample rs : table) {
			s += rs.rank + " ";
			s += Arrays.toString(rs.sample.F()) + "\n";
		}
		return s;
	}
	
	private class RankedSample {
		public int rank;
		public Sample sample;
		
		public RankedSample(Sample s) {
			rank = 0;
			sample = s;
		}
	}
	
	private class SampleColumnComparator implements Comparator<RankedSample> {
		private int column;
		
		public  SampleColumnComparator(int col) {
			column = col;
		}

		@Override
		public int compare(RankedSample o1, RankedSample o2) {
			double d = o1.sample.F()[column] - o2.sample.F()[column];
			if(d > 0) {
				return 1;
			} else if(d < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	private class RankComparator implements Comparator<RankedSample> {
		@Override
		public int compare(RankedSample o1, RankedSample o2) {
			return o1.rank - o2.rank;
		}
	}
}
