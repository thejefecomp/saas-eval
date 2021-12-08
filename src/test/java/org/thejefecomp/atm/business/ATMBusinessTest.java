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


import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thejefecomp.atm.business.exception.NoSufficientCashException;
import org.thejefecomp.atm.business.exception.SuitableBankNotesNotesNotAvailableException;
import org.thejefecomp.atm.model.BankNote;
import org.thejefecomp.atm.storage.ATMStorage;

public class ATMBusinessTest{
	
	ATMStorage atmStorage;

	ATMBusiness atmBusiness;
	
	@Before
	public void setUp() {
		
		this.atmStorage = new ATMStorage();
		
		this.atmBusiness = new ATMBusiness(this.atmStorage);
	}
	
	@After
	public void tearDown() {
		
		this.atmStorage = null;
		
		this.atmBusiness = null;
	}
	
	@Test
	public void insertBankNotesTest() {
		
		//Insert test 1
		this.atmBusiness.insertBankNote(50, 1);
		this.atmBusiness.insertBankNote(20, 3);
		this.atmBusiness.insertBankNote(10, 5);
		
		Assert.assertFalse(this.atmStorage.getAtmBankNoteList().isEmpty());
		Assert.assertEquals(1L, this.atmStorage
								    .getAtmBankNoteList()
								    .stream()
								    .filter(bankNote -> bankNote.getValue() == 50 &&
								    					bankNote.getQuantity() == 1)
								    .count());
		
		Assert.assertEquals(1L, this.atmStorage
				   					.getAtmBankNoteList()
									.stream()
									.filter(bankNote -> bankNote.getValue() == 20 &&
														bankNote.getQuantity() == 3)
									.count());
		
		Assert.assertEquals(1L, this.atmStorage
									.getAtmBankNoteList()
									.stream()
									.filter(bankNote -> bankNote.getValue() ==10 &&
														bankNote.getQuantity() == 5)
									.count());
		
		Assert.assertEquals(160L, atmStorage.getTotalValue());
		
		//Insert test 2
		this.atmBusiness.insertBankNote(50, 10);
		Assert.assertEquals(1L, this.atmStorage
			    .getAtmBankNoteList()
			    .stream()
			    .filter(bankNote -> bankNote.getValue() == 50)
			    .filter(bankNote -> bankNote.getQuantity() == 11)
			    .count());
		
		Assert.assertEquals(660L, atmStorage.getTotalValue());
	}
	
    @Test
    public void withdrawSuccessTest(){
    	
    	this.insertBankNotesTest();
    	
    	try {
    		
    		List<BankNote> withdrawBankNoteList = this.atmBusiness.withdraw(80);
    		
    		Assert.assertEquals(3, withdrawBankNoteList.size());
    		Assert.assertEquals(3L, withdrawBankNoteList.stream()
    													.filter(bankNote -> (bankNote.getValue() == 50 && 
    																		 bankNote.getQuantity() == 1) ||
    																		 (bankNote.getValue() == 20 && 
    																		  bankNote.getQuantity() == 1) ||
    																		 (bankNote.getValue() == 10 && 
    																		 bankNote.getQuantity() == 1))
    													.count());
    		Assert.assertEquals(1L, this.atmStorage
    			    .getAtmBankNoteList()
    			    .stream()
    			    .filter(bankNote -> bankNote.getValue() == 50 &&
    			    					bankNote.getQuantity() == 10)
    			    .count());
    		
    		Assert.assertEquals(1L, this.atmStorage
   					.getAtmBankNoteList()
					.stream()
					.filter(bankNote -> bankNote.getValue() == 20 &&
										bankNote.getQuantity() == 2)
					.count());
    		
    		Assert.assertEquals(1L, this.atmStorage
					.getAtmBankNoteList()
					.stream()
					.filter(bankNote -> bankNote.getValue() == 10 &&
										bankNote.getQuantity() == 4)
					.count());
    		
    		Assert.assertEquals(580L, this.atmStorage.getTotalValue());
    	}
    	catch(NoSufficientCashException | SuitableBankNotesNotesNotAvailableException e){
    		
    		Assert.fail();
    	}	
    }
    
    @Test
    public void withdrawSuccessTest1(){
    	
try {
    		
    		this.atmBusiness.insertBankNote(50, 2);
    		
    		List<BankNote> withdrawBankNoteList = this.atmBusiness.withdraw(50);
    		
    		
    		Assert.assertEquals(1, withdrawBankNoteList.size());
    		
    		Assert.assertEquals(1L, withdrawBankNoteList.stream()
    			    									.filter(bankNote -> bankNote.getValue() == 50 &&
    			    														bankNote.getQuantity() == 1)
    			    									.count());
    		
    		Assert.assertEquals(1L, this.atmStorage
    			    .getAtmBankNoteList()
    			    .stream()
    			    .filter(bankNote -> bankNote.getValue() == 50 &&
    			    					bankNote.getQuantity() == 1)
    			    .count());
    		
    		Assert.assertEquals(50L, this.atmStorage.getTotalValue());
    		
    		withdrawBankNoteList = this.atmBusiness.withdraw(50);
    		
    		
    		Assert.assertEquals(1, withdrawBankNoteList.size());
    		
    		Assert.assertEquals(1L, withdrawBankNoteList.stream()
    			    									.filter(bankNote -> bankNote.getValue() == 50 &&
    			    														bankNote.getQuantity() == 1)
    			    									.count());
    		
    		Assert.assertEquals(1L, this.atmStorage
    			    .getAtmBankNoteList()
    			    .stream()
    			    .filter(bankNote -> bankNote.getValue() == 50 &&
    			    					bankNote.getQuantity() == 0)
    			    .count());
    		
    		Assert.assertEquals(0L, this.atmStorage.getTotalValue());
    		
    		
    	}
    	catch(NoSufficientCashException | SuitableBankNotesNotesNotAvailableException e){
    		
    		Assert.fail();
    	}	
    }
    
    @Test
    public void withdrawFailureTestTotal(){
    	
    	try {
    		
    		this.insertBankNotesTest();
    		
    		this.atmBusiness.withdraw(700);
    		
    		Assert.fail();
    	}
    	catch (NoSufficientCashException e) {
    		
    		Assert.assertTrue(e.getMessage(), true);
    		
    	}
    	catch(SuitableBankNotesNotesNotAvailableException e){
    		
    		Assert.fail();
    	}
    }
    
    @Test
    public void withdrawFailureTestBankNotes1(){
    	
    	try {
    		
    		this.atmBusiness.insertBankNote(50, 2);
    		
    		this.atmBusiness.withdraw(60);
    		
    		Assert.fail();		
    	}
    	catch (NoSufficientCashException e) {
    		
    		Assert.fail();
    	}
    	catch(SuitableBankNotesNotesNotAvailableException e){
    		
    		Assert.assertTrue(e.getMessage(), true);
    	}
    }
    
    @Test
    public void withdrawFailureTestBankNotes2(){
    	
    	try {
    		
    		this.atmBusiness.insertBankNote(50, 2);
    		this.atmBusiness.insertBankNote(20, 1);
    		
    		this.atmBusiness.withdraw(80);
    		
    		Assert.fail();		
    	}
    	catch (NoSufficientCashException e) {
    		
    		Assert.fail();
    	}
    	catch(SuitableBankNotesNotesNotAvailableException e){
    		
    		Assert.assertTrue(e.getMessage(), true);
    	}
    }
    
    @Test
    public void rollbackTest(){
    	
    	try {
    		
    		this.atmBusiness.insertBankNote(50, 2);
    		this.atmBusiness.insertBankNote(20, 2);
    		
    		this.atmBusiness.withdraw(130);
    		
    		Assert.fail();		
    	}
    	catch (NoSufficientCashException e) {
    		
    		Assert.fail();
    	}
    	catch(SuitableBankNotesNotesNotAvailableException e){
    	
    		Assert.assertEquals(1L, this.atmStorage
    			    					.getAtmBankNoteList()
    			    					.stream()
    			    					.filter(bankNote -> bankNote.getValue() == 50 &&
    			    										bankNote.getQuantity() == 2)
    			    					.count());
    		
    		Assert.assertEquals(1L, this.atmStorage
   										.getAtmBankNoteList()
   										.stream()
   										.filter(bankNote -> bankNote.getValue() == 20 &&
   															bankNote.getQuantity() == 2)
   										.count());
    		
    		Assert.assertEquals(140L, atmStorage.getTotalValue());
    	}
    }
}
