package com.gct.entity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "glpi_computers")
public class Computer {
	@Id
	@Column(name = "id")
	private int id;
	@Column(name = "serial")
	private String serial;
	@Column(name = "contact")
	private String contact;
	@Column(name = "name")
	private String name;
	/**
	 * @return
	 */
	public String getSerial() {
		return serial;
	}
	/**
	 * @param serial
	 */
	public void setSerial(String serial) {
		this.serial = serial;
	}
	/**
	 * @return
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return
	 */
	public String getContact() {
		return contact;
	}
	/**
	 * @param contact
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

}
