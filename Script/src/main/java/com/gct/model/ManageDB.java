package com.gct.model;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import com.gct.entity.Computer;

/**
 * ManageDB.java A class that corrects information on database based on Excel file
 * @author mbedoui
 *
 */
public class ManageDB {
	private static final String FILE_PATH = "/home/mbedoui/Téléchargements/affec_pc.xlsx";
	static String pathFile="" ;
	static Computer computerObj;
	static Session sessionObj;
	static SessionFactory sessionFactoryObj;
	static Configuration configObj ;
	/**
	 * main method 
	 * @param args
	 */
	public static void main(String args[]) {
		String pathFile=args[0];
		List computerList =getComputerListFromExcel(pathFile);
		System.out.println(""+computerList.size()+computerList.get(0).toString());
		List<Computer> computers=getComputerListFromDB();
		correctDataFromFileToDB(computerList);
		correctDataReferToName(computers);


	}
	/**
	 * @param pathFile
	 * @return List of computer based on Excel File
	 */
	private static List getComputerListFromExcel(String pathFile) {
		List computerList = new ArrayList();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(pathFile);
			// Using XSSF for xlsx format, for xls use HSSF
			Workbook workbook = new XSSFWorkbook(fis);
			int numberOfSheets = workbook.getNumberOfSheets();
			//looping over each workbook sheet
			for (int i = 0; i < 1; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				Iterator rowIterator = sheet.iterator();
				//iterating over each row
				while (rowIterator.hasNext()) {
					ComputerModel computer = null ; 
					Row row = (Row) rowIterator.next();
					Iterator cellIterator = row.cellIterator();
					//Iterating over each cell (column wise)  in a particular row.
					while (cellIterator.hasNext()) {
						Cell cell = (Cell) cellIterator.next(); 
						// started from 16 Row to get content of computer object
						if(row.getRowNum()>15){
							if (Cell.CELL_TYPE_STRING == cell.getCellType()) { 
								if (cell.getColumnIndex() == 3) {
									computer.setComputerName(cell.getStringCellValue());
								}
							} else if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
								if (cell.getColumnIndex() == 0) {
									String _value = NumberToTextConverter.toText(cell.getNumericCellValue());
									computer =new ComputerModel();
									computer.setSerialNumber(_value);
								}

								else if (cell.getColumnIndex() == 2) {
									computer.setOwner(Double.toString(cell.getNumericCellValue()).substring(0,5));
								}
								
							}
						}

					}
					//end iterating a row, add all the elements of a row in list
					if(computer != null && (computer.getOwner() != null ))
						computerList.add(computer);
				}
			} 

			fis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return computerList;
	}
	/**
	 * @return a List of computers 
	 */
	private static List <Computer> getComputerListFromDB() {
		List <Computer> computers =null ;
		 try {
				getSessionObj().beginTransaction();
				SQLQuery sqlquery=getSessionObj().createSQLQuery("SELECT * from glpi_computers");
				List<Computer> list = (List<Computer>)sqlquery.list();
				computers= list;
			} catch(Exception sqlException) {
				sqlException.printStackTrace();
			} finally {
				getSessionObj().close();
			}

         return computers ;
	}
	/**
	 * @param computerList
	 */
	private static void correctDataFromFileToDB(List  computerList){
		//iterate over the file and in every record check the line in db
		// update the field
		try {
			sessionObj = buildSessionFactory().openSession();
			// Getting Transaction Object From Session Object
			sessionObj.beginTransaction();
			for (Iterator iterator = computerList.iterator(); iterator.hasNext();) {
				ComputerModel computer = (ComputerModel) iterator.next();
				//for every Row we will adapt serial number
				correctCurrentRow(computer);
			}
			sessionObj.getTransaction().commit();
		} catch(Exception sqlException) {
			if(null != sessionObj.getTransaction()) {
				sessionObj.getTransaction().rollback();
			}
			sqlException.printStackTrace();
		} finally {
			if(sessionObj != null) {
				sessionObj.close();
			}
		}


	}
	/**
	 * @param computer
	 */
	private static void correctCurrentRow(ComputerModel computer ){
		if(computer.getOwner() != null){
			SQLQuery sqlquery=sessionObj.createSQLQuery("select *  from glpi_computers  where contact="+computer.getOwner()+" and operatingsystemversions_id ="+1);
			sqlquery.addEntity(Computer.class);
			Object c= sqlquery.uniqueResult();
			if(c != null){
				((Computer)c).setSerial(computer.getSerialNumber());
				System.out.println("setSerial "+computer.getSerialNumber()+" for  "+computer.getOwner());
				sessionObj.save(c);
			}else {
				if(computer.getComputerName().toUpperCase().split(" ").length==2){
					sqlquery=sessionObj.createSQLQuery("select *  from glpi_computers  where name like '%"+computer.getComputerName().toUpperCase().split(" ")[0]+"%' and operatingsystemversions_id ="+1);
                
				}
				//take attention of user name format BEN AAA AAA
				else if(computer.getComputerName().toUpperCase().split(" ").length==3){
					sqlquery=sessionObj.createSQLQuery("select *  from glpi_computers  where name like '%"+computer.getComputerName().toUpperCase().split(" ")[1]+"%' and operatingsystemversions_id ="+1);

				}

				sqlquery.addEntity(Computer.class);
				if(sqlquery.list().size()> 0){
					c=  sqlquery.list().get(0) ;
					if ( c != null ){
						((Computer)c).setSerial(computer.getSerialNumber());
						System.out.println("setSerial c is  null"+computer.getSerialNumber()+" for  "+computer.getOwner());
						sessionObj.save(c);
					}
				}


			}
		}

	}
	private static void correctDataReferToName(List<Computer> computerList){
		 try {
			// Getting Session Object From SessionFactory
				sessionObj = buildSessionFactory().openSession();
				// Getting Transaction Object From Session Object
				sessionObj.beginTransaction();
				List<Computer> list= new ArrayList<Computer>(); 
				Iterator it = computerList.iterator();
				while(it.hasNext()){		
			         Object line = it.next();
					 Object []	temp= (Object[]) line ;     
				     Computer comp =new Computer();
				     if(temp[1] !=null && temp[2] !=null){
				    	 comp.setContact(temp[1].toString());
					     comp.setName(temp[2].toString());
					     list.add(comp);
					     getCustomRow(comp);
				     }
				     
				     
				}
				
		 } catch(Exception sqlException) {

				sqlException.printStackTrace();
			} finally {
				//if(sessionObj != null && sessionObj.isOpen()) {
				//	sessionObj.close();
				//}
				}

				
	}
	private static int getCustomRow(Computer computer ){

		if(computer.getContact().equals("NULL")){
			//fetch the data 
			ComputerModel computerDetailsFromFile =getCorrectNameFromFile(computer.getName());
			try {
				Computer c =new Computer();
				SQLQuery sqlquery=sessionObj.createSQLQuery("select *  from glpi_computers  where contact="+computerDetailsFromFile.getOwner());
				sqlquery.addEntity(Computer.class);
				Object cmp= sqlquery.uniqueResult();
				if(cmp != null ){
					((Computer)cmp).setSerial(computerDetailsFromFile.getSerialNumber());
					((Computer)cmp).setContact(computerDetailsFromFile.getOwner());
					sessionObj.update(cmp);
				}else{
					proceedUncompleteData(computer.getName().split("-")[2]);

				}

			} catch(Exception sqlException) {
				if(null != sessionObj.getTransaction()) {
					System.out.println("\n.......Transaction Is Being Rolled Back.......");
					
				}
				sqlException.printStackTrace();
			} finally {
			}}

		return 1 ;
	}
	private static void proceedUncompleteData(String computerUser){
		List computerList = new ArrayList();
		computerList=getComputerListFromExcel(pathFile);
		ComputerModel cmp =retrivedMissedData(computerUser,computerList);
		performTable(cmp);
	}
	private static ComputerModel retrivedMissedData(String computerUser,List computerList){
		for (Iterator iterator = computerList.iterator(); iterator.hasNext();) {
			ComputerModel computer = (ComputerModel) iterator.next();
			if(computer.getComputerName().toUpperCase().startsWith(computerUser))
			{
							return computer ;
			}

		}
		return  null ;
	}
	private static void performTable(ComputerModel computer){
		 try {
				Computer c =new Computer();
				SQLQuery sqlquery=sessionObj.createSQLQuery("select *  from glpi_computers  where name like '%"+computer.getComputerName().toUpperCase().split(" ")[0]+"%'");
				sqlquery.addEntity(Computer.class);
				Object cmp= sqlquery.uniqueResult();
			if(cmp != null ){
				((Computer)cmp).setSerial(computer.getSerialNumber());
				((Computer)cmp).setContact(computer.getOwner());
			 	sessionObj.update(cmp);
			 	sessionObj.getTransaction().commit();
			}else{
				
			}
			
		 } catch(Exception sqlException) {
				if(null != sessionObj.getTransaction()) {
					System.out.println("\n.......Transaction Is Being Rolled Back.......");
					sessionObj.getTransaction().rollback();
				}
				sqlException.printStackTrace();
			} finally {
//				if(sessionObj != null && sessionObj.isOpen()) {
//					sessionObj.close();
//				}
				
			}
	}
	private static ComputerModel getCorrectNameFromFile(String computerName){
		 String [] arrOfcomputerName =computerName.split("-");
		 return fecthDataFromFile(arrOfcomputerName[2]);
		
	}
	private static ComputerModel  fecthDataFromFile(String splittedComputerName){

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(pathFile);

			// Using XSSF for xlsx format, for xls use HSSF
			Workbook workbook = new XSSFWorkbook(fis);

			int numberOfSheets = workbook.getNumberOfSheets();
           
			//looping over each workbook sheet
			for (int i = 0; i < 1; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				Iterator rowIterator = sheet.iterator();

				//iterating over each row
				while (rowIterator.hasNext()) {

					ComputerModel computer = new ComputerModel() ; 
					Row row = (Row) rowIterator.next();
					Iterator cellIterator = row.cellIterator();

					//Iterating over each cell (column wise)  in a particular row.
					while (cellIterator.hasNext()) {
						Cell cell = (Cell) cellIterator.next(); 
						//The Cell Containing String will is name.
						if(row.getRowNum()>15){
							if (Cell.CELL_TYPE_STRING == cell.getCellType()) { 
								//	computer.setComputerName(cell.getStringCellValue());
								if (cell.getColumnIndex() == 3) {
									computer.setComputerName(cell.getStringCellValue());
									String [] m =computer.getComputerName().split(" ");
									if(m[0].equalsIgnoreCase(splittedComputerName)){
										return computer ;
										
									}
								}
								//The Cell Containing numeric value will contain marks
							} else if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
								//String serialNumber=Double.toString(cell.getNumericCellValue()).substring(6, 9);  
								//  
								//Cell with index 1 contains marks in Maths
								if (cell.getColumnIndex() == 0) {
									String serialNumber =Double.toString(cell.getNumericCellValue()).substring(6, 9);
									computer.setSerialNumber("17100"+serialNumber);
									
								}
								//Cell with index 2 contains marks in SciencebeginIndex
								else if (cell.getColumnIndex() == 2) {
									computer.setOwner(Double.toString(cell.getNumericCellValue()).substring(0,5));
								}
								//Cell with index 3 contains marks in English
								else if (cell.getColumnIndex() == 3) {
									computer.setComputerName(cell.getStringCellValue());
								}
							}
						}


					} 

					fis.close();
				}}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static Session getSessionObj(){
		if(null !=sessionObj ){
			return sessionObj;
		}else{
			return buildSessionFactory().openSession();
		}

	}
	private static SessionFactory buildSessionFactory() {
		// Creating Configuration Instance & Passing Hibernate Configuration File
		if(sessionFactoryObj==null){
			configObj= new Configuration();
			configObj.configure("hibernate.cfg.xml");
			// Since Hibernate Version 4.x, ServiceRegistry Is Being Used
			ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder().applySettings(configObj.getProperties()).build(); 
			// Creating Hibernate SessionFactory Instance
			sessionFactoryObj = configObj.buildSessionFactory(serviceRegistryObj);
		}
		return sessionFactoryObj;
	}

}
