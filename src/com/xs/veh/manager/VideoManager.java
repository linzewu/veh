package com.xs.veh.manager;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.VideoConfig;

@Service("videoManager")
public class VideoManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public List getProcessDataByLsh(final String jylsh) {
		return hibernateTemplate.execute(new HibernateCallback<List<Map>>() {
			@Override
			public List<Map> doInHibernate(Session session) throws HibernateException {
				List<Map> list = session
						.createSQLQuery(
								"SELECT * FROM VEH_IS_PROCSTATUS WHERE JYLSH=:jylsh   AND JYXM!='00' AND JYZT='2' AND JLZT='1' AND KSSJ IS NOT NULL  AND JSSJ IS NOT NULL ")
						.setParameter("jylsh", jylsh).setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP).list();

				return list;
			}
		});
	}
	
	public void saveConfig(VideoConfig vc){
		this.hibernateTemplate.merge(vc);
	}
	
	public List<VideoConfig> getConfig(String jcjgdh){
		
		String sql ="FROM VideoConfig";
		List<VideoConfig> datas=null;
		if(jcjgdh!=null&&!jcjgdh.equals("")){
			sql+=" where jyjgbh=?";
			datas = (List<VideoConfig>) this.hibernateTemplate.find(sql, jcjgdh);
		}else{
			datas = (List<VideoConfig>) this.hibernateTemplate.find(sql);
		}
		return datas;
	}
	
	public List<VideoConfig> getConfig(String jcjgdh,String jcxdh){
		
		String sql ="FROM VideoConfig where jyjgbh=? and jcxdh=?";
		List<VideoConfig> datas=null;
		datas = (List<VideoConfig>) this.hibernateTemplate.find(sql, jcjgdh,jcxdh);
		return datas;
	}
	
	
	
	public void deleteConfig(VideoConfig vc){
		this.hibernateTemplate.delete(vc);
	}

}
