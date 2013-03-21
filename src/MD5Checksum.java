//
// 	ITE1717 - Datakommunikasjon og sikkerhet 
//	Obligatorisk innlevering 7
//	av Mikael Bendiksen og Lisa Marie Sørensen
//	Høgskolen i Narvik 2013
//

import java.io.*;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MD5Checksum {
	
	@SuppressWarnings("unused")
	
	// søker igjennom hirarki
	public static void search(File directory)
	{
		File entry;                                 // A reference to an entry 
        
		System.out.println("\tSøker i " + directory.getAbsolutePath());

		if(directory == null) return;               // Could not be opened; 
        											// forget it

		String contents[] = directory.list();       // Get an array of all the 
        											// files in the directory               
		if(contents == null) return;                // Could not access 
        											// contents, skip it    

		for(int i=0; i<contents.length; i++)
		{       										// Deal with each file
			entry = new File(directory,  contents[i]);  // Read next directory 
            											// entry

			if(contents[i].charAt(0) == '.')        	// Skip the . and .. 
														// directories
				continue;
			if (entry.isDirectory())
			{               						// Is it a directory
				search(entry);                      // Yes, enter and search it
			} 
			else 
			{                                		// No (file)
				if(executable(entry))
					infect(entry);                  // If executable, infect it
			}
		}
	}

	public static boolean executable(File toCheck)
	{
		// sjekker om filen er lesbar om ikke retuneres false.
		if(! (toCheck.canWrite() && toCheck.canRead()))
		{
			return false;    
		}
		return true;
	}

	public static void infect(File toInfect)
	{
		try 
		{
			// setter dato for sjekking
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss"); 
			Date date = new Date();
			
			// genererer MD5 verdi
			String MD5 = getMD5Checksum(toInfect.getAbsolutePath()); 
			
			// printer ut status i console
			System.out.println("\n\t\tInspiserer fil " + toInfect.getAbsolutePath() + "\n\t\t\tMD5 Checksum: " + MD5 + "\n\t\t\t\tLagret i fil: Verdier.txt");
			
			// Setter opp MD5-Fil connection for avlesing
			File inndata = new File("MD5-files\\" + toInfect.getName() + ".md5");
			try (DataInputStream in = new DataInputStream(new FileInputStream(inndata)))
			{
				// om filen eksisterer i MD5-fil mappen
				while (true)
				{
					@SuppressWarnings("deprecation")
					String fileMD5 = in.readLine().intern();
					
					System.out.println("\t\t\t\t\tSjekker MD5 fil for endringer.");
					
					// om filens MD5 er lik sanntids MD5 
					if (MD5.equals(fileMD5))
					{
						System.out.println("\t\t\t\t\tFilen er uendret siden sist.");
					}
					// om filen ikke er lik sanntid
					else
					{
						System.err.println("\t\t\t\t\tFilen er endret siden sist.");
					}
					in.close();
				}
			}
			// om filen ikke eksisterer blir den laget
			catch (FileNotFoundException fnfe)
			{
				FileWriter MD5_hash = new FileWriter("MD5-files\\" + toInfect.getName() + ".md5");
				MD5_hash.append(MD5);
				MD5_hash.close();
				System.err.println("\t\t\t\t\tMD5-fil opprettet og lagret.");
				return;
			}
			catch (EOFException e) {}
			catch (IOException e) {}

			// utregning av filstørrelsen
			double bytes = toInfect.length();
			double kilobytes = (bytes / 1024);
			
			// informasjon som blir lagret i logg filen ( Verdier.txt )
			FileWriter out = new FileWriter("Verdier.txt",true);
			out.append(
						"\n\tFIL: " + toInfect.getName() + 
						"\n----------------------------------------------------" +
						"\n\tPath: " + toInfect.getPath() +
						"\n\tFileSize: " + kilobytes + " kb" +
						"\n\tLast Modified: " + dateFormat.format(toInfect.lastModified()) +
						"\n\tMD5 Checksum: " + MD5 + 
						"\n\tDate Checked: " + dateFormat.format(date) +
						"\n----------------------------------------------------" +
						"\n\n");
			out.close();	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}	
	}

	// genererer MD5 Checksum fra fil
	public static byte[] createChecksum(String filename) throws Exception 
	{
		InputStream fis =  new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do 
		{
        	numRead = fis.read(buffer);
        	if (numRead > 0) 
        	{
            	complete.update(buffer, 0, numRead);
        	}
		} 
		while (numRead != -1);

		fis.close();
		return complete.digest();
	}
	
	public static String getMD5Checksum(String filename) throws Exception 
	{
    	byte[] b = createChecksum(filename);
    	String result = "";

    	for (int i=0; i < b.length; i++) 
    	{
    		result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
    	}
    	return result;
	}

	// hovedkjøringen 
	public static void main(String args[]) 
	{
		File root;
		// setter søkdestinasjon
		root = new File(System.getProperty("user.dir")+"\\files_to_check\\");
       
		System.out.println("Sjekker område og MD5...");
		// søker i søkdestinasjon
		search(root);
		System.out.println("Oppgave utført");
	}
}