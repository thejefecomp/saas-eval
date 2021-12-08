package org.thejefecomp.atm.business;

/******************************************************************************
*    Copyright (c)  2021, Jeferson Souza (thejefecomp) - 
*    thejefecomp@neartword.com. All rights reserved. 
*
*    Redistribution and use in source and binary forms, with or without 
*    modification, are permitted provided that the following conditions are 
*    met: 
*
*    1. Redistributions of source code must retain the above copyright notice,
*       this list of conditions and the following disclaimer. 
*
*    2. Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the 
*       documentation and/or other materials provided with the distribution. 
*
*    3. All advertising materials mentioning features or use of this software
*       must display the following acknowledgement: This product includes
*       software developed by Jeferson Souza (thejefecomp). 
*
*    4. Neither the name of Jeferson Souza (thejefecomp) nor the names of its
*       contributors may be used to endorse or promote products derived from 
*       this software without specific prior written permission.
*
*    THIS SOFTWARE IS PROVIDED BY JEFERSON SOUZA (THEJEFECOMP) "AS IS" AND ANY
*    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
*    DISCLAIMED. IN NO EVENT SHALL JEFERSON SOUZA (THEJEFECOMP) BE LIABLE FOR
*    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
*    DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
*    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
*    CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
*    LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
*    OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
*    SUCH DAMAGE.
******************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.thejefecomp.atm.business.exception.NoSufficientCashException;
import org.thejefecomp.atm.business.exception.SuitableBankNotesNotesNotAvailableException;
import org.thejefecomp.atm.model.BankNote;
import org.thejefecomp.atm.storage.ATMStorage;


/*
 * This class specifies the business rules associated with
 * ATM operation. In the scope of the implementation, only
 * the withdraw operation, and its associated supporting
 * methods are covered.
 */
public class ATMBusiness {
	
	private ATMStorage atmStorage;
	
	private Comparator<BankNote> bankNoteCmp = (bn1,bn2) -> bn1.getValue() > 
															bn2.getValue() ? -1 
																		   : 1;
	
	private boolean isStorageSorted = false;
	
	
	/*
	 * Performs a rollback operation in case of an unsuccessful withdraw attempt.
	 */
	private void rollbackPreviousWithdrawAttempt(List<BankNote> withdrawBankNoteList) {
		
		withdrawBankNoteList.stream()
						    .forEach(bankNoteWithdraw ->{
												
								     Optional<BankNote> bankNoteATMOpt = this.atmStorage.getAtmBankNoteList().stream()
								    		 																 .filter(bankNoteATM -> bankNoteATM.getValue() == bankNoteWithdraw.getValue())
								    		 																 .findFirst();
								    BankNote bankNoteATM = bankNoteATMOpt.get();		 
								    bankNoteATM.setQuantity(bankNoteATM.getQuantity() + bankNoteWithdraw.getQuantity());
								    
								    this.atmStorage.setTotalValue(this.atmStorage.getTotalValue()+bankNoteWithdraw.getValue() * bankNoteWithdraw.getQuantity());
								    
							});	
	}
	
	public ATMBusiness(ATMStorage atmStorage) {
		
		this.atmStorage = atmStorage;
	}
	
	/*
	 * Inserts a bank note with its associated quantity in
	 * the ATM machine.
	 */
	public void insertBankNote(int value, int quantity) {
			
		Optional<BankNote> bankNoteOpt = this.atmStorage
										  .getAtmBankNoteList()
										  .stream()
										  .filter(bankNote -> bankNote.getValue() == value)
										  .findFirst();
		
		if (bankNoteOpt.isPresent()) {
			BankNote targetBankNote = bankNoteOpt.get();
			targetBankNote.setQuantity(targetBankNote.getQuantity() + quantity);
		}
		else {
			this.atmStorage.getAtmBankNoteList().add(new BankNote(value,quantity));
		}
		
		this.atmStorage.setTotalValue(this.atmStorage.getTotalValue() + value * quantity);
		
		this.isStorageSorted = false;
	}
	
	/*
	 * Iterative method to perform withdraw operations based on a sorted list, 
	 * which checks for all possible combinations of the available bank notes, 
	 * returning the first matching the requested money, or throwing an exception 
	 * in accordance with what was specified in the SAAS implementation assignment. 
	 */
	public List<BankNote> withdraw(int value) throws NoSufficientCashException,
													 SuitableBankNotesNotesNotAvailableException{
		
		if (value > this.atmStorage.getTotalValue()) {
			
			throw new NoSufficientCashException();
		}
		
		if(!isStorageSorted) {
			
			Collections.sort(this.atmStorage.getAtmBankNoteList(),this.bankNoteCmp);
			
			this.isStorageSorted = true;
		}
		
		List<BankNote> withdrawBankNoteList = new ArrayList<>();
		
		int accumulatedValue = 0;
		
		int size = this.atmStorage.getAtmBankNoteList().size();
		
		for(int i = 0; i < size; i++) {
			
			withdrawBankNoteList.clear();
			
			accumulatedValue = 0;
		
			for(BankNote bankNote : this.atmStorage.getAtmBankNoteList().subList(i, size)) {
			
				int maxValueObtained = bankNote.getValue() * bankNote.getQuantity();				
						
				if((accumulatedValue + maxValueObtained) <= value) {
					
					withdrawBankNoteList.add(new BankNote(bankNote.getValue(),bankNote.getQuantity()));
					bankNote.setQuantity(0);
					accumulatedValue += maxValueObtained;
					this.atmStorage.setTotalValue(this.atmStorage.getTotalValue()-maxValueObtained);
				}
				else if((accumulatedValue + bankNote.getValue()) <= value){
					
					int bankNotesCounter = 0;
					
					while(bankNote.getQuantity() > 1 && 
						  bankNotesCounter <= bankNote.getQuantity() &&
						  (accumulatedValue + bankNote.getValue()) <= value) {
					
						accumulatedValue += bankNote.getValue();
						this.atmStorage.setTotalValue(this.atmStorage.getTotalValue()-bankNote.getValue());
						bankNotesCounter++;
					}
					
					bankNotesCounter = bankNotesCounter == 0 ? 1 : bankNotesCounter;
					
					withdrawBankNoteList.add(new BankNote(bankNote.getValue(),bankNotesCounter));
					bankNote.setQuantity(bankNote.getQuantity()-bankNotesCounter);
				}
				
				if (accumulatedValue == value)
					return withdrawBankNoteList;	
			}
			
			this.rollbackPreviousWithdrawAttempt(withdrawBankNoteList);	
		}
		
		throw new SuitableBankNotesNotesNotAvailableException();
	}
}
