public class A2q5 {
	
	double[][] table = new double['z'+1]['z'+1];
	
	public A2q5 () {
		table['a']['a'] = -0.740;
		table['a']['c'] = 0.419;
		table['a']['g'] = 0.580;
		table['a']['t'] = -0.803;
		table['c']['t'] = -0.913;
		table['c']['c'] = 0.302;
		table['c']['g'] = 1.812;
		table['c']['t'] = -0.685;
		table['g']['a'] = -0.624;
		table['g']['c'] = 0.461;
		table['g']['g'] = 0.331;
		table['g']['t'] = -0.730;
		table['t']['t'] = -1.169;
		table['t']['c'] = 0.573;
		table['t']['g'] = 0.393;
		table['t']['t'] = -0.679;
	}
	

	public static void main(String[] args) {
		System.out.println ("Hello World!");
	}

}
