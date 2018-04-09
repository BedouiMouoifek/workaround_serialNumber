package com.gct.model;


/**
 * @author mbedoui
 *
 */
public class ComputerModel {
	@Override
	public String toString() {
		return "Computer [serialNumber=" + serialNumber + ", computerName="
				+ computerName + ", owner=" + owner + "]";
	}
	private String serialNumber ;
	/**
	 * @return
	 */
	public String getSerialNumber() {
		return serialNumber;
	}
	/**
	 * @param serialNumber
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	/**
	 * @return
	 */
	public String getComputerName() {
		return computerName;
	}
	/**
	 * @param computerName
	 */
	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}
	private String computerName ;
	private String owner;
	/**
	 * @return
	 */
	public String getOwner() {
		return owner;
	}
	/**
	 * @param serialNumber
	 * @param computerName
	 * @param owner
	 */
	public ComputerModel(String serialNumber, String computerName, String owner) {
		super();
		this.serialNumber = serialNumber;
		this.computerName = computerName;
		this.owner = owner;
	}
	/**
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	/**
	 * @param serialNumber
	 * @param computerName
	 */
	public ComputerModel(String serialNumber, String computerName) {
		super();
		this.serialNumber = serialNumber;
		this.computerName = computerName;
	}
	/**
	 * 
	 */
	public ComputerModel() {
		super();
	}
	
	

}
