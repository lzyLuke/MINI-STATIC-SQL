package tests;

import static org.junit.Assert.*;

import java.io.IOException;

import nio.BinaryTupleWriter;
import nio.NormalTupleReader;
import nio.TupleReader;

import org.junit.Test;

import util.Tuple;

public class BinaryTupleWriterTest {

	@Test
	public void test() {
		try{
			TupleReader reader = new
					NormalTupleReader("sandbox/Boats_humanreadable");
			BinaryTupleWriter writer = new BinaryTupleWriter("sandBox/HMY");		

		Tuple t;	
			while( ( t = reader.read())!=null){
					writer.write(t);
				
				
			}
			writer.close();
			
		
		}catch(IOException e){
			e.printStackTrace();
		}
	
		
	}
}
