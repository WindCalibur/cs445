import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Arrays;

// Global alignment algorithm using affine gap penalty - general algorithm

public class gpa3m {
	String mSeqA;
	String mSeqB;
	
	// most recent
	static int maxI;
	static int maxJ;
	
	static String S1;
	static String S2;
	
	// ...
	int[][] M,Gx,Gy;
	static int[][] sim;
	int mScore;
	String mAlignmentSeqA = "";
	String mAlignmentSeqB = "";
	int gapd,gape;
	final static Charset ENCODING = StandardCharsets.US_ASCII;
	final static String delims = "[ ]+";
	final static int NEGINF = Integer.MIN_VALUE/2;

	static void init_sim(String matrix_name) throws IOException {
		sim = new int[256][256];
		for (int i=0; i<256; i++)
			for (int j=0; j<256; j++)
				sim[i][j] = 0;
		Path path = Paths.get(matrix_name);
		try (Scanner scanner = new Scanner(path,ENCODING.name())) {
			String line;
			do {
				line = scanner.nextLine();
			} while (line.charAt(0)=='#');
			String[] letters = line.split(delims);
			int map[];
			map = new int[21];
			for (int j=1; j<=20; j++) {
				map[j] = letters[j].charAt(0);
				sim[0][map[j]] = 1;
			}
			for (int i=0; i<20; i++) {
				String[] scores = scanner.nextLine().split(delims);
				int offset = 0;
				while (scores[offset].isEmpty())
					offset++;
				int row = scores[offset].charAt(0);
				for (int j=1; j<=20; j++) {
					sim[row][map[j]] = Integer.parseInt(scores[offset+j]);
				}
			}
			// copy C to U
			sim[0]['U'] = 1;
			for (int j=1; j<=20; j++) {
				sim['U'][map[j]] = sim['C'][map[j]];
				sim[map[j]]['U'] = sim[map[j]]['C'];
			}
		}
	}

	static String readName(Scanner scanner) {
		if (scanner.hasNextLine()) {
			String[] parts = scanner.nextLine().split(delims);
			String[] subparts = parts[0].split("\\|");
			return subparts[2];
		}
		return null;
	}

	static String readSequence(Scanner scanner) {
		String output = new String();
		while (scanner.hasNextLine() && scanner.findInLine(">") == null)
			output += scanner.nextLine();
		return output;
	}

	static boolean checkSequence(String seq) {
		for (int i=0; i<seq.length(); i++)
			if (sim[0][seq.charAt(i)] == 0) {
				System.out.println("Illegal character: "+seq.charAt(i));
				return false;
			}
		return true;
	}

	void init(String seqA, String seqB) {
		mSeqA = seqA;
		mSeqB = seqB;
		mAlignmentSeqA = "";
		mAlignmentSeqB = "";
		M = new int[mSeqA.length() + 1][mSeqB.length() + 1];
		Gx = new int[mSeqA.length() + 1][mSeqB.length() + 1];
		Gy = new int[mSeqA.length() + 1][mSeqB.length() + 1];
		M[0][0] = 0;
		Gx[0][0] = NEGINF;
		Gy[0][0] = NEGINF;
		for (int i = 1; i <= mSeqA.length(); i++) {
			M[i][0] = 0;
			Gx[i][0] = -gapd;
			Gy[i][0] = NEGINF;
		}

		for (int j = 1; j <= mSeqB.length(); j++) {
			M[0][j] = 0;
			Gx[0][j] = NEGINF;
			Gy[0][j] = -gapd;
		}
	}

	int process() {
		for (int i = 1; i <= mSeqA.length(); i++) {
			for (int j = 1; j <= mSeqB.length(); j++) {

				M[i][j] = M[i-1][j-1];
				if (Gx[i-1][j-1] > M[i][j])
					M[i][j] = Gx[i-1][j-1];
				if (Gy[i-1][j-1] > M[i][j])
					M[i][j] = Gy[i-1][j-1];
				M[i][j] += weight(i,j);
				M[i][j] = Math.max(M[i][j], 0);

				Gx[i][j] = M[i-1][j] - gapd;
				if (Gx[i-1][j] - gape > Gx[i][j])
					Gx[i][j] = Gx[i-1][j] - gape;
				if (Gy[i-1][j] - gapd > Gx[i][j])
					Gx[i][j] = Gy[i-1][j] - gapd;

				Gy[i][j] = M[i][j-1] - gapd;
				if (Gx[i][j-1] - gapd > Gy[i][j])
					Gy[i][j] = Gx[i][j-1] - gapd;
				if (Gy[i][j-1] - gape > Gy[i][j])
					Gy[i][j] = Gy[i][j-1] - gape;
			}
		}

//		int result = M[mSeqA.length()][mSeqB.length()];
//		if (Gx[mSeqA.length()][mSeqB.length()] > result)
//			result = Gx[mSeqA.length()][mSeqB.length()];
//		if (Gy[mSeqA.length()][mSeqB.length()] > result)
//			result = Gy[mSeqA.length()][mSeqB.length()];
		
		int score = 0;
		for (int i = 0; i < M.length; i++) {
		    for (int j = 0; j < M[i].length; j++) {
		        if (M[i][j] > score) {
		        	maxI = i;
		        	maxJ = j;
		           score = M[i][j];
		        }
		    }
		}
		return score;
	}

	
	int[] traceback(String seqA, String seqB) {
		// changes S1, S2
		
		int currentI = maxI;
		int currentJ = maxJ;
		S1 = "";
		S2 = "";

		S1 = S1 + seqA.charAt(currentI-1);
		S2 = S2 + seqB.charAt(currentJ-1);
		
		currentI--;
		currentJ--;
		int length = 1;
		
		// 0 = M, 1 = Gx, 2 = Gy
		int state = 0;
		while (true) {
			int currentM = M[currentI][currentJ];
			int currentY = Gy[currentI][currentJ];
			int currentX = Gx[currentI][currentJ];
			if (state == 1) {
				currentM = M[currentI][currentJ] - gapd;
				currentY = Gy[currentI][currentJ] - gapd;
				currentX = Gx[currentI][currentJ] - gape;
			} else if (state == 2) {
				currentM = M[currentI][currentJ] - gapd;
				currentY = Gy[currentI][currentJ] - gape;
				currentX = Gx[currentI][currentJ] - gapd;
			}
			if ((currentM == 0) && (currentM == Math.max(currentY, Math.max(currentX, currentM)))) {
				break;
			}
			length++;
			if ((currentM >= currentY) && (currentM >= currentX)) {
				S1 = seqA.charAt(currentI-1) + S1;
				S2 = seqB.charAt(currentJ-1) + S2;
				currentI--;
				currentJ--;
				state = 0;
			} else if ((currentY >= currentX) && (currentY >= currentM)) {
				S1 = '-' + S1;
				S2 = seqB.charAt(currentJ-1) + S2;
				currentJ--;
				state = 2;
			} else if ((currentX >= currentY) && (currentX >= currentM)) {
				S1 = seqA.charAt(currentI-1) + S1;
				S2 = '-' + S2;
				currentI--;
				state = 1;
			}
		}
		int[] result = {currentI+1, currentJ+1, length};
		return result;
	}
	 

	private int weight(int i, int j) {
		return sim[mSeqA.charAt(i-1)][mSeqB.charAt(j-1)];
	}

	void printMatrix() {
		System.out.println("M =");
		for (int i = 0; i < mSeqA.length() + 1; i++) {
			for (int j = 0; j < mSeqB.length() + 1; j++) {
				System.out.print(String.format("%4d ", M[i][j]));
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Gx =");
		for (int i = 0; i < mSeqA.length() + 1; i++) {
			for (int j = 0; j < mSeqB.length() + 1; j++) {
				System.out.print(String.format("%4d ", Gx[i][j]));
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Gy =");
		for (int i = 0; i < mSeqA.length() + 1; i++) {
			for (int j = 0; j < mSeqB.length() + 1; j++) {
				System.out.print(String.format("%4d ", Gy[i][j]));
			}
			System.out.println();
		}
		System.out.println();
	}

	void printScoreAndAlignments() {
		System.out.println("Score: " + mScore);
		System.out.println("Sequence A: " + mAlignmentSeqB);
		System.out.println("Sequence B: " + mAlignmentSeqA);
		System.out.println();
	}

	gpa3m(int gapd,int gape) {
		this.gapd = gapd;
		this.gape = gape;
	}
	
	public static void main(String [] args) {
		
		class Record implements Comparable<Record> {
			public int index;
			public String name;
			public String sequence;
			public int score;
			int length;
			int[][] M,Gx,Gy;
			int scoreI;
			int scoreJ;
			String firstSequence;
			String secondSequence;
			public int compareTo(Record r) {
				return r.score - this.score;
			}
		};
		
		String query;
		Record[] record = new Record[1017];
		try {
			init_sim("BLOSUM50.txt");
			Path path = Paths.get("unknown.txt");
			Scanner scanner = new Scanner(path,ENCODING.name());
//			query = readSequence(scanner);
//			if (!checkSequence(query))
//				System.out.println("Query sequence contains illegal character");
			path = Paths.get("2017-01-16 uniprot.fasta");
			scanner = new Scanner(path,ENCODING.name());
			int maxlength = 0, maxlengthi = -1;
			
			for (int i=0; i<record.length; i++) {
				record[i] = new Record();
				record[i].index = i;
				record[i].name = readName(scanner);
				record[i].sequence = readSequence(scanner);
				if (!checkSequence(record[i].sequence))
					System.out.println(record[i].sequence+" contains illegal character");
				else
					if (record[i].sequence.length() > maxlength) {
						maxlength = record[i].sequence.length();
						maxlengthi = i;
					}
			}
			query = record[1000].sequence;
			System.out.println(maxlengthi+" L="+maxlength);
		} catch (IOException e) {
			System.out.println("Cannot find file "+e.toString());
			return;
		}

		gpa3m nw = new gpa3m(10,5);

		// Sample
		System.out.println("Query="+record[59].name);
		// for (int i=0; i<50; i++) {
		nw.init(record[30].sequence, record[59].sequence);
		record[30].score = nw.process();
		int[] result = nw.traceback(record[30].sequence, record[59].sequence);
		record[30].scoreI = result[0];
		record[30].scoreJ = result[1];
		record[30].length = result[2];
		record[30].firstSequence = S1;
		record[30].secondSequence = S2;
		
		nw.init(record[33].sequence, record[59].sequence);
		record[33].score = nw.process();
		result = nw.traceback(record[33].sequence, record[59].sequence);
		record[33].scoreI = result[0];
		record[33].scoreJ = result[1];
		record[33].length = result[2];
		record[33].firstSequence = S1;
		record[33].secondSequence = S2;
		
		nw.init(record[48].sequence, record[59].sequence);
		record[48].score = nw.process();
		result = nw.traceback(record[48].sequence,record[59].sequence);
		record[48].scoreI = result[0];
		record[48].scoreJ = result[1];
		record[48].length = result[2];
		record[48].firstSequence = S1;
		record[48].secondSequence = S2;
		// }

		// Arrays.sort(record,0,50);
		System.out.println("Best 3 in sample:");
		int currentIndex = 30;
		
		System.out.println("Index="+record[currentIndex].index+" Name="+record[currentIndex].name+" Score="+record[currentIndex].score);
		System.out.println("START POS in "+ record[currentIndex].name +": "+record[currentIndex].scoreI+" START POS in "
				+record[59].name+": "+record[currentIndex].scoreJ +" LENGTH: "+record[currentIndex].length 
				+ "\n" + record[currentIndex].firstSequence + "\n" + record[currentIndex].secondSequence);

		currentIndex = 33;
		System.out.println("Index="+record[currentIndex].index+" Name="+record[currentIndex].name+" Score="+record[currentIndex].score);
		System.out.println("START POS in "+ record[currentIndex].name +": "+record[currentIndex].scoreI+" START POS in "
				+record[59].name+": "+record[currentIndex].scoreJ +" LENGTH: "+record[currentIndex].length 
				+ "\n" + record[currentIndex].firstSequence + "\n" + record[currentIndex].secondSequence);
		
		currentIndex = 48;
		System.out.println("Index="+record[currentIndex].index+" Name="+record[currentIndex].name+" Score="+record[currentIndex].score);
		System.out.println("START POS in "+ record[currentIndex].name +": "+record[currentIndex].scoreI+" START POS in "
				+record[59].name+": "+record[currentIndex].scoreJ +" LENGTH: "+record[currentIndex].length 
				+ "\n" + record[currentIndex].firstSequence + "\n" + record[currentIndex].secondSequence);
		
		// Real instance
		for (int i=0; i<1000; i++) {
			//System.out.println(i);
			nw.init(record[i].sequence, query);
			record[i].score = nw.process();
			result = nw.traceback(record[i].sequence,query);
			record[i].scoreI = result[0];
			record[i].scoreJ = result[1];
			record[i].length = result[2];
			record[i].firstSequence = S1;
			record[i].secondSequence = S2;
		}

		Arrays.sort(record);
		System.out.println("Best 3 in real instance:");
		for (int i=0; i<3; i++) {
			System.out.println("Index="+record[i].index+" Name="+record[i].name+" Score="+record[i].score);
			System.out.println("START POS in "+ record[i].name +": "+record[i].scoreI+" START POS in "
					+record[59].name+": "+record[i].scoreJ +" LENGTH: "+record[i].length + "\n" + record[currentIndex].firstSequence + 
					"\n" + record[currentIndex].secondSequence);
		}

	/*
      nw.init(record[0].sequence, query);
      nw.process();
      nw.traceback();
      System.out.println(nw.mAlignmentSeqA);
      System.out.println(nw.mAlignmentSeqB);
		 */
	}
}
