package api.dtos;

import java.math.BigDecimal;

public class CurrencyConversionDto {
	
	//CurrencyExchange
	private String from;
	private String to;
	private BigDecimal exchangeValue;
	private String environment;
	
	//CurrencyConversion
	private BigDecimal conversionTotal;
	private Double quantity;

	
	public CurrencyConversionDto() {
		
	}


	public String getFrom() {
		return from;
	}


	public void setFrom(String from) {
		this.from = from;
	}


	public String getTo() {
		return to;
	}


	public void setTo(String to) {
		this.to = to;
	}


	public BigDecimal getExchangeValue() {
		return exchangeValue;
	}


	public void setExchangeValue(BigDecimal exchangeValue) {
		this.exchangeValue = exchangeValue;
	}


	public String getEnvironment() {
		return environment;
	}


	public void setEnvironment(String environment) {
		this.environment = environment;
	}


	public BigDecimal getConversionTotal() {
		return conversionTotal;
	}


	public void setConversionTotal(BigDecimal conversionTotal) {
		this.conversionTotal = conversionTotal;
	}


	public Double getQuantity() {
		return quantity;
	}


	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	
	
}
