package org.agung.sshtunnel.addon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.programmerplanet.sshtunnel.model.Tunnel;

/**
 * 
 * @author <a href="agungm@outlook.com">Mulya Agung</a>
 */

public class CsvConfigImporter {
    public static final String COMMA_DELIMITER = ",";
	
	public final List<String> tunnelConfHeaders = new ArrayList<String>(
			Arrays.asList("localAddress", "localPort", "remoteAddress", "remotePort", "type"));
	
	public Set<Tunnel> readCsv(String csvPath) {
		HashSet<Tunnel> importedTunnels = null;
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csvPath));
			// Get header
			String line = br.readLine();
			if (line != null) {
				String[] colNamesArr = line.split(COMMA_DELIMITER);
				List<String> colNames = Arrays.asList(colNamesArr);
				//Collections.sort(colNames);
				if (colNames.equals(tunnelConfHeaders)) {
					importedTunnels = new HashSet<Tunnel>();
					while ((line = br.readLine()) != null) {
				        String[] values = line.split(COMMA_DELIMITER);
				        //records.add(Arrays.asList(values));
				        if (values.length == tunnelConfHeaders.size()) {
				        	Tunnel tunnel = new Tunnel();
				        	tunnel.setLocalAddress(values[0]);
				        	tunnel.setLocalPort(Integer.parseInt(values[1]));
				        	tunnel.setRemoteAddress(values[2]);
				        	tunnel.setRemotePort(Integer.parseInt(values[3]));
				        	tunnel.setLocal(values[4].toLowerCase().equals("local"));
				        	importedTunnels.add(tunnel);
				        }
//				        else {
//				        	System.out.println("WARNING: skipped invalid line");
//				        }
				    }
				}
//				else {
//					error = new Exception("Invalid header!\nColumn names should be " + 
//							Arrays.toString(tunnelConfHeaders.toArray()));
//				}
			}
//			else {
//				error = new Exception("Header must not be empty");
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return importedTunnels;
	}
	
	public String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(","));
	}
	
	public String escapeSpecialCharacters(String data) {
	    if (data == null) {
	        throw new IllegalArgumentException("Input data cannot be null");
	    }
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
	        data = data.replace("\"", "\"\"");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}
	
	public void writeCsv(List<Tunnel> tunnels, String csvPath) throws Exception {
		List<String[]> dataLines = new ArrayList<>();
		// Add header
		dataLines.add(tunnelConfHeaders.toArray(new String[0]));
		
		for (Tunnel tunnel: tunnels) {
			String[] line = new String[tunnelConfHeaders.size()];
			line[0] = tunnel.getLocalAddress();
			line[1] = String.valueOf(tunnel.getLocalPort());
			line[2] = tunnel.getRemoteAddress();
			line[3] = String.valueOf(tunnel.getRemotePort());
			if (tunnel.getLocal())
				line[4] = "local";
			else
				line[4] = "remote";
			dataLines.add(line);
		}
		try (PrintWriter pw = new PrintWriter(new File(csvPath))) {
			dataLines.stream().map(this::convertToCSV).forEach(pw::println);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
}
