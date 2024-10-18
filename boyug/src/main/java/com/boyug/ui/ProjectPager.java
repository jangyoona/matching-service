package com.boyug.ui;

public class ProjectPager {

	private int pageSize;//한 페이지당 데이터 개수
	private int pagerSize;//번호로 보여주는 페이지 Link 개수
	private int dataCount;//총 데이터 수

	private int pageNo;//현재 페이지 번호
	private int pageCount;//총 페이지 수

	private String linkUrl;//페이저가 포함되는 페이지의 주소

	private String queryString = "";


	public ProjectPager(int dataCount, int pageNo, int pageSize, int pagerSize, String linkUrl, String queryString) {
		
		this.linkUrl = linkUrl;
		
		this.dataCount = dataCount;
		this.pageSize = pageSize;
		this.pagerSize = pagerSize;
		this.pageNo = pageNo;		
		pageCount = (dataCount / pageSize) + ((dataCount % pageSize) > 0 ? 1 : 0);
		
		if (queryString == null || queryString.trim().length() == 0) {
			this.queryString = "";
		} else {
			String[] items = queryString.split("&");
			for (int idx = 0; idx < items.length; idx++) {
				if (!items[idx].startsWith("pageNo")) {					
					this.queryString += "&" + items[idx];
				}
			}
		}
	}
	
	public String toString(){
		StringBuffer linkString = new StringBuffer(2048);
		
		//1. 처음, 이전 항목 만들기
		if (pageNo > 1) {
			linkString.append("<li class='page-item'> " +
							"<a class='page-link' href='#' aria-label='Previous'>" +
							"<span aria-hidden='true'>&laquo;</span>" +
							"</a>" +
							"</li>");
			linkString.append("<li class='page-item'><a class='page-link' href='javascript:'>Previous</a></li>");
		} else {
			linkString.append("<li class='page-item disabled'> " +
							"<a class='page-link' href='javascript:' aria-label='Previous'>" +
							"<span aria-hidden='true'>&laquo;</span>" +
							"</a>" +
							"</li>");
			linkString.append("<li class='page-item disabled'><a class='page-link' href='javascript:'>Previous</a></li>");
		}
		
		//2. 페이지 번호 Link 만들기
		int pagerBlock = (pageNo - 1) / pagerSize;
		int start = (pagerBlock * pagerSize) + 1;
		int end = start + pagerSize;
		for (int i = start; i < end; i++) {
			if (i > pageCount) break;
			if (i == pageNo) {
				linkString.append(String.format(
						"<li class='page-item'><a class='page-link' style='background-color: gray; color: white;' href='javascript:'>%d</a></li>", i));
			} else {
				linkString.append(String.format(
						"<li class='page-item'><a class='page-link' href='javascript:'>%d</a></li>", i));
			}
		}
		
		//3. 다음, 마지막 항목 만들기
		if (pageNo < pageCount) {
			linkString.append("<li class='page-item'><a class='page-link' href='javascript:'>Next</a></li>");
			linkString.append("<li class='page-item'>" +
				"<a class='page-link' href='javascript:' aria-label='Next'>" +
				"<span aria-hidden='true'>&raquo;</span>" +
				"</a>" +
				"</li>");
		} else {
			linkString.append("<li class='page-item disabled'><a class='page-link' href='javascript:' aria-label='Next'>Next</a></li>");
			linkString.append("<li class='page-item disabled'>" +
					"<a class='page-link' href='javascript:' aria-label='Next'>" +
					"<span aria-hidden='true'>&raquo;</span>" +
					"</a>" +
					"</li>");
		}
		
		return linkString.toString();
	}

}












