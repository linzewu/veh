package com.xs.veh.network.data;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 测功机
 * @author linzewu
 *
 */
@Scope("prototype")
@Component("dynoData")
@Entity
@Table(name = "TM_DynoData")
public class DynoData  extends BaseDeviceData{
	
	@Override
	public void setZpd() {
		// TODO Auto-generated method stub
	}

}
