package com.xs.veh.entity;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("user")
@Entity
@Table(name = "TB_User")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler","password"})
@NamedQueries({
	@NamedQuery(name = "User.login", query = "from User u where u.userName=:userName  and u.userState<>:userState") 
})
public class User extends BaseEntity {
	
	public final static Integer USER_STATE_PASSWORD_INVALID=0;
	
	public final static Integer USER_STATE_NORMAL=1; 
	
	public final static String User_type_dly="1";
	
	public final static String User_type_ycy="2";
	
	public static final String SYSTEM_USER="system";
	
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
	
	@Column
	private Integer userState;
	
	//登录IP
	@Column(length=160)
	private String loginIP;
	
	//账号有效截止日期
	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")  
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date userNameValidDate;
	
	//密码有效截止日期
	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")  
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date pwValidDate;
	
	//允许登录时间(开始)
	@Column(length=10)
	private String permitBeginTime;
	
	//允许登录时间(截止)
	@Column(length=10)
	private String permitEndTime;
	
	@Column
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date lastLoginDate;
	
	@Column
	private Character isPolice;
	
	@Column(length=40)
	private String employeeNumber;
	
	@Column
	private Integer loginFailCou;
	
	@Column(length=10)
	private String userType;
	
	//本次登陆ip
	@Column(length=30)
	private String ip;
	
	//上次登录的时间
	@Column
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date lastTimeLoginDate;
	
	//上次登录的IP终端
	@Column(length=30)
	private String lastTimeIP;
	
	//上次登录失败的时间
	@Column
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date lastTimeLoginFailDate;
	
	//上次登录失败的IP终端
	@Column(length=30)
	private String lastTimeLoginFailIP;
	
	@Transient
	private String pwOverdue;
	
	@Transient
	private String roleName;
	
	
	


	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getLoginIP() {
		return loginIP;
	}

	public Date getUserNameValidDate() {
		return userNameValidDate;
	}

	public Date getPwValidDate() {
		return pwValidDate;
	}

	public String getPermitBeginTime() {
		return permitBeginTime;
	}

	public String getPermitEndTime() {
		return permitEndTime;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}


	public String getEmployeeNumber() {
		return employeeNumber;
	}

	public void setLoginIP(String loginIP) {
		this.loginIP = loginIP;
	}

	public void setUserNameValidDate(Date userNameValidDate) {
		this.userNameValidDate = userNameValidDate;
	}

	public void setPwValidDate(Date pwValidDate) {
		this.pwValidDate = pwValidDate;
	}

	public void setPermitBeginTime(String permitBeginTime) {
		this.permitBeginTime = permitBeginTime;
	}

	public void setPermitEndTime(String permitEndTime) {
		this.permitEndTime = permitEndTime;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}


	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public Integer getUserState() {
		return userState;
	}

	public void setUserState(Integer userState) {
		this.userState = userState;
	}

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

	public Character getIsPolice() {
		return isPolice;
	}

	public void setIsPolice(Character isPolice) {
		this.isPolice = isPolice;
	}

	public String getPwOverdue() {
		return pwOverdue;
	}

	public void setPwOverdue(String pwOverdue) {
		this.pwOverdue = pwOverdue;
	}

	public Integer getLoginFailCou() {
		return loginFailCou;
	}

	public void setLoginFailCou(Integer loginFailCou) {
		this.loginFailCou = loginFailCou;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}	

	public Date getLastTimeLoginDate() {
		return lastTimeLoginDate;
	}

	public void setLastTimeLoginDate(Date lastTimeLoginDate) {
		this.lastTimeLoginDate = lastTimeLoginDate;
	}	

	public String getIp() {
		return ip;
	}

	public String getLastTimeIP() {
		return lastTimeIP;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setLastTimeIP(String lastTimeIP) {
		this.lastTimeIP = lastTimeIP;
	}

	public Date getLastTimeLoginFailDate() {
		return lastTimeLoginFailDate;
	}

	public String getLastTimeLoginFailIP() {
		return lastTimeLoginFailIP;
	}

	public void setLastTimeLoginFailDate(Date lastTimeLoginFailDate) {
		this.lastTimeLoginFailDate = lastTimeLoginFailDate;
	}

	public void setLastTimeLoginFailIP(String lastTimeLoginFailIP) {
		this.lastTimeLoginFailIP = lastTimeLoginFailIP;
	}

	public String encodePwd(String password) {
		if(StringUtils.isEmpty(password)) {
			return null;
		}else {
			try {
				return md5(this.getId().toString()+password+this.realName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public static void main(String[] age) {
		User user =new User();
		user.setId(1);
		user.setRealName("管理员");
		user.setPassword("888888");
		System.out.println(user.encodePwd("888888"));
		
	}
	

}
