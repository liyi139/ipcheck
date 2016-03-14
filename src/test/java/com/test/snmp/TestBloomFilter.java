package com.test.snmp;

import java.util.Date;
import java.util.Random;

import org.junit.Test;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class TestBloomFilter {
	@Test
	public void test(){
		BloomFilter<Long> bloomFilter = BloomFilter.create(Funnels.longFunnel(), 50000000, 0.01F);  
		Random ra =new Random();
		long start=Runtime.getRuntime().freeMemory();
		for(long i=0;i<50000000;i++){
			if(i%1000000==0){
				long end=Runtime.getRuntime().freeMemory();
				System.out.println((start-end));
			}
			bloomFilter.put(ra.nextLong());
		}
		long end=Runtime.getRuntime().freeMemory();
		System.out.println((start-end));
		long startTime=new Date().getTime();
		System.out.println(bloomFilter.mightContain(423456664545l));
		long endTime=new Date().getTime();
		System.out.println("========="+((endTime-startTime)/1000));
		
	}
}
