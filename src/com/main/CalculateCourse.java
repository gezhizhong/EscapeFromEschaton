package com.main;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CalculateCourse {

	private static final String inputFile = "C:\\EclipseWorkSpace\\EscapeFromEschaton\\chart.txt";
	private static List<Asteroid> listAsteroid;
	private static List<Course> listCourse;               
	private static int blastMove;
	private static int time;
	private static int position;
	private static int velocity;
	private static int acceleration;
	
	CalculateCourse(){
		listAsteroid = new ArrayList<Asteroid>();
		listCourse = new ArrayList<Course>();
		blastMove = 0;
		time = 0;
		position = 0;
		velocity = 0;
		acceleration = 0;
	}

	private boolean checkBlast(int time, int nextPos){
		
		int blastPos = time / blastMove;
		
		if(blastPos == nextPos){
			//System.out.println("checkBlast>" + time + ">" + nextPos);
			return false;
		}
		
		return true;
	}

	private boolean checkAsteroid(int time, int nextPos){
		
		Asteroid as = listAsteroid.get(nextPos-1);
		if(as != null){
			if((as.getPerCycle() - as.getOffset()) > time){
				return true;
			}else{		
				if((time - (as.getPerCycle() - as.getOffset())) % as.getPerCycle() == 0){
					//System.out.println("checkAsteroid>" + time + ">" + nextPos);
					return false;
				}
			}
		}
		
		return true;
	}

	private boolean checkEschaton(int time, int nextPos){
		
		if (time < blastMove && nextPos == -1){
			//System.out.println("checkEschaton>" + time + ">" + nextPos);
			return false;
		}
		
		return true;
	}

	private void readChartFromJSON() {

		JSONParser parser = new JSONParser();

		try {

			Object obj = parser.parse(new FileReader(inputFile));

			JSONObject jsonObject = (JSONObject) obj;

			String blastMoveStr = Long.toString((long)jsonObject.get("t_per_blast_move"));
			blastMove = Integer.parseInt(blastMoveStr);
			//System.out.println("blastMove->" + blastMove);

			JSONArray asteroids = (JSONArray) jsonObject.get("asteroids");
			Iterator i = asteroids.iterator();
			int count = 0;
			while (i.hasNext()) {
				JSONObject innerObj = (JSONObject) i.next();
				
				String offset = Long.toString((long)innerObj.get("offset"));
				String cycleSpeed = Long.toString((long)innerObj.get("t_per_asteroid_cycle"));
				
				//System.out.println("offset->" + innerObj.get("offset"));
				//System.out.println("t_per_asteroid_cycle->" + innerObj.get("t_per_asteroid_cycle"));
				
				Asteroid as = new Asteroid();
				as.setOffset(Integer.parseInt(offset));
				as.setPerCycle(Integer.parseInt(cycleSpeed));
				
				listAsteroid.add(count, as);				
				count ++;
			}

		} catch (Exception ex) {
			System.out.println("readChartFromJOSN>>" + ex);
		}
		
	}

	private boolean findNextInCourse(){
		
		if(checkEschaton(time, position) && checkBlast(time, position) && checkAsteroid(time, position)){
			Course course = new Course();
			course.setAcceleration(acceleration);
			course.setPosition(position);
			course.setVelocity(velocity);
			//System.out.println("findNextInCourse>" + time + ">" + acceleration + ">" + position + ">" + velocity);
			listCourse.add(time, course);
			
			time ++;
			
			return true;
		}else{
			return false;
		}
	}
	
	private void calculateParameters(int acc){
		acceleration = acc;
		velocity = velocity + acceleration;
		position = position + velocity;
	}
	
	private void changeTactics(){
		
		if(acceleration > -1){
			calculateParameters(acceleration --);;
		}else{
			
			time --;		
			Course course = listCourse.get(time);	
			acceleration = course.getAcceleration();
			position = course.getPosition();
			velocity = course.getVelocity();
			listCourse.remove(time);
			changeTactics();
		}
	}

	private void findCourse(){

		calculateParameters(1);
		
		while(true){

			if (position > listAsteroid.size()){
				break;
			}
			
			if(findNextInCourse()){
				calculateParameters(1);
			}else{
				changeTactics();
			}
		}
	}
	
	private String writeCourseToJSON(){
		
		String course = "";
		if(listCourse != null){
			for(int i = 0; i < listCourse.size(); i++){
				Course c = listCourse.get(i);
				if(course.equals("")){
					course = "[" + c.getAcceleration();
				}else{
					course = course +  ", " + c.getAcceleration();
				}
			}
			course = course + "]";
		}
		
		return course;
	}

	public static void main(String[] args) {

		CalculateCourse cc = new CalculateCourse();
		// 1. Read chart
		cc.readChartFromJSON();

		// 2. Recurse to get the escape course
		cc.findCourse();

		// 3. Write course
		System.out.println(cc.writeCourseToJSON());

	}

}
