package org.test.paydatecalculator.common;

import java.util.Date;

import org.test.paydatecalculator.utils.DateUtil;

/**
 * Class for container objects used to pass results of computations between
 * {@link PaymentDateCalculator} and {@link DateWriter} objects. This class does
 * not have any logic attached to it so it relies on external logic meaning that
 * when using this class the code needs to ensure that the values of both Date
 * objects are within the same month.
 *
 * @author Florin Matei
 *
 */
public class PaymentMonth {
	private final Date salaryDate;
	private final Date bonusDate;
	private final String monthName;

	/**
	 * {@link PaymentMonth} constructor. IllegalArgumentException will be thrown
	 * if both salaryDate and bonusDate are null. (The reason for this is that
	 * if that happens then the month needs to be null so there would be no need
	 * for this object)
	 *
	 * @param monthName
	 * @param salaryDate
	 * @param bonusDate
	 */
	public PaymentMonth(final String monthName,
			final Date salaryDate,
			final Date bonusDate) {
		if ((salaryDate == null) && (bonusDate == null)) {
			throw new IllegalArgumentException("At least one date needs to be non-null");
		}
		if (monthName == null) {
			throw new IllegalArgumentException("Month name cannot be null");
		}
		this.monthName = monthName;
		this.salaryDate = salaryDate;
		this.bonusDate = bonusDate;
	}

	public Date getBonusDate() {
		return this.bonusDate;
	}

	public Date getSalaryDate() {
		return this.salaryDate;
	}

	public boolean hasBonusDate() {
		return this.bonusDate != null;
	}

	public boolean hasSalaryDate() {
		return this.salaryDate != null;
	}

	public String getMonthName() {
		return this.monthName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result)
				+ ((this.bonusDate == null) ? 0 : this.bonusDate.hashCode());
		result = (prime * result) + this.monthName.hashCode();
		result = (prime * result)
				+ ((this.salaryDate == null) ? 0 : this.salaryDate.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (obj instanceof PaymentMonth) {
			final PaymentMonth object = (PaymentMonth) obj;

			boolean equals = this.monthName.equals(object.monthName);
			equals &= DateUtil.datesEqual(this.salaryDate, object.salaryDate);
			equals &= DateUtil.datesEqual(this.bonusDate, object.bonusDate);
			return equals;
		}

		return false;
	}

	@Override
	public String toString() {
		return this.monthName + " Salary Date: " + this.salaryDate.toString()
				+ " Bonus Date: " + this.bonusDate;
	}
}
