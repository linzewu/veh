package com.xs.veh.util;

import org.hibernate.Query;

public class PageInfo {
	
	private Integer pageNumber;
	
	private Integer pageSize;
	

	public Integer getFirstRowNumber() {
		if(pageSize!=null&&pageNumber!=null)
			return (pageNumber-1)*pageSize;
		else
			return null;
	}


	public Integer getPageNumber() {
		return pageNumber;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	
	public Query toPage(Query query){
		
		if(getFirstRowNumber()!=null){
			query.setFirstResult(getFirstRowNumber());
			query.setMaxResults(this.getPageSize());
		}
		
		return query;
	}

}
