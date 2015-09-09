package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("user")
@Entity
@Table(name = "TB_User")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler","password"})
@NamedQueries({
	@NamedQuery(name = "User.login", query = "from User u where u.userName=:userName and u.password=:password") 
})
public class User extends BaseEntity {
	
	@Column(unique=true,length=32)
	@Pattern(regexp="^[a-zA-Z\\d]\\w{3,11}[a-zA-Z\\d]$",message="{user.userFomatterError}")
	private String userName;
	
	@Column(length=32)
//	@NotNull(message="{user.password.notNull}")
	private String password;
	
	@Column(length=30)
	private String realName;
	
	
	@Column(length=64)
	@Pattern(regexp = "^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$", message = "{user.idFomatterError}")
	private String idCard;
	
	@Column
	private Integer roleId; 
	
	
	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	

}
