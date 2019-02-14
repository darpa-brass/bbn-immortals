package com.securboration.client;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.example.burgershake.Burger;
import org.example.burgershake.Meal;
import org.example.burgershake.Shake;
import org.example.burgershake.ShakeTopping;

public class RandomMealBuilder{
	public static Meal randomMeal(Random rng) throws DatatypeConfigurationException {
    	Meal meal = new Meal();
    	
    	final int numBurgers = rng.nextInt(10);
    	for(int i=0;i<numBurgers;i++) {
    		meal.getBurger().add(randomBurger(rng));
    	}
    	meal.setShake(randomShake(rng));
    	return meal;
    }
	
	private static Burger randomBurger(Random rng) throws DatatypeConfigurationException {
		Burger b = new Burger();
		
		b.setBurgerId("burger-" + rng.nextInt(Integer.MAX_VALUE));
		
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date(rng.nextInt(60*60*24*365*50)*1000L));
			b.setEatenTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		}
		b.setNumPatties(rng.nextInt(10));
		
		return b;
	}
	
	private static Shake randomShake(Random rng) {
		Shake s = new Shake();
		
		s.setShakeName("shake-"+rng.nextInt(Integer.MAX_VALUE));
		final int numToppings = rng.nextInt(4);
		for(int i=0;i<numToppings;i++) {
			s.getToppings().add(ShakeTopping.values()[rng.nextInt(ShakeTopping.values().length)]);
		}
		
		return s;
	}
}
