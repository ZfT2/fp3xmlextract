package de.zft2.fp3xmlextract.convert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zft2.fp3xmlextract.config.Fp3xmlextractProperties;
import de.zft2.fp3xmlextract.data.BankAccount;
import de.zft2.fp3xmlextract.exception.ConfigurationException;

public class AccountProcessor {
	
	private static Logger log = LogManager.getLogger(AccountProcessor.class);

	protected static Fp3xmlextractProperties propsTransfer;
	protected static Fp3xmlextractProperties propsAccount;
	protected static Fp3xmlextractProperties propsSkip;
	
	protected static Map<String,Collection<String>> accountNumbersMap = new HashMap<>();

	protected AccountProcessor() throws ConfigurationException {
		initProperties();
		setAccountNumbersMap();
	}

	private static void initProperties() throws ConfigurationException {
		if (propsTransfer == null)
			propsTransfer = Fp3xmlextractProperties.getInstance("transfer.properties", true);
		if (propsAccount == null)
			propsAccount = Fp3xmlextractProperties.getInstance("account.properties", true);
		if (propsSkip == null)
			propsSkip = Fp3xmlextractProperties.getInstance("skip.properties", false);
	}
	
	private void setAccountNumbersMap() {
		for (Map.Entry<Object, Object> property : propsTransfer.entrySet()) {

			String[] possibleIdentifiers = ((String) property.getValue()).split(";");
			String accountNamePP = ((String) property.getKey());

			accountNumbersMap.put(accountNamePP, Arrays.asList(possibleIdentifiers));
		}
	}

	protected String findAccountNamePP(String accountIdentifier) {

		for (Map.Entry<Object, Object> property : propsTransfer.entrySet()) {

			String[] possibleIdentifiers = ((String) property.getValue()).split(";");
			String accountNamePP = ((String) property.getKey());

			for (String identifier : possibleIdentifiers) {
				if (identifier.equalsIgnoreCase(accountIdentifier)) {
					return accountNamePP;
				}
			}
		}
		return null;
	}
	
	protected boolean setupTransferProperties(Collection<BankAccount> accountList) {
		
		boolean result = false;
		
		if (!propsTransfer.isEmpty()) {
			log.info("Transfer properties found, size: {}", propsTransfer.size());
		}
		log.info("creating default transfer properties...");
		for (BankAccount account : accountList) {
			String accountDescription = account.getBezeichnung();
			if (propsTransfer.get(accountDescription) != null) {
				log.info("Found already transfer property for account {} in file, so skipping default.", accountDescription);
				continue;
			}
			Set<String> identifiersSet = new HashSet<>();
			if (account.getIban() != null) {
				identifiersSet.add(account.getIban());
				if (account.getIban().length() > 10) {
					identifiersSet.add(account.getIban().substring(account.getIban().length() - 10));
				}
			}
			if (account.getNumber() != null) {
				identifiersSet.add(account.getNumber());
				identifiersSet.add(account.getNumber().replaceFirst("^0+(?!$)", ""));
			}
			
			/*List<String>*/ Stream<Object> allAccountIdentifiersFromProp = accountNumbersMap.entrySet().stream().flatMap(e -> e.getValue().stream())/*.collect(Collectors.toList())*/;
			if (!identifiersSet.isEmpty() && allAccountIdentifiersFromProp.noneMatch(identifiersSet::contains)) {
				propsTransfer.put(accountDescription, String.join(";", identifiersSet));
				result = true;
			}
		}

		for (Map.Entry<Object, Object> property : propsAccount.entrySet()) {
			String possibleIdentifiers = ((String) property.getValue());
			String accountNamePP = ((String) property.getKey());
			propsTransfer.put(accountNamePP, possibleIdentifiers);
		}

		log.info("created/added default transfer properties with size: {}", propsTransfer.size());
		return result;
	}

	public void addParentAccounts(Collection<BankAccount> accountList) {
		for (Entry<String, Collection<String>> entry : accountNumbersMap.entrySet()) {
			BankAccount accountFound = null;
			for (BankAccount account : accountList) {
				for (String identifier : entry.getValue()) {
					if (identifier.equalsIgnoreCase(account.getIban()) || identifier.equalsIgnoreCase(account.getNumber())) {
						if (accountFound != null) {
							accountFound.setParentAccount(entry.getKey());
							account.setParentAccount(entry.getKey());
						} else {
							accountFound = account;
						}
					}
				}
			}
		}
	}

}
