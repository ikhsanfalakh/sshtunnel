package org.agung.sshtunnel.addon

import org.programmerplanet.sshtunnel.model.Tunnel
import java.io.*
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 *
 * @author [Mulya Agung](agungm@outlook.com)
 */
class CsvConfigImporter {
    val tunnelConfHeaders: List<String?> = ArrayList(
        mutableListOf<String?>("localAddress", "localPort", "remoteAddress", "remotePort", "type")
    )

    @Throws(IOException::class)
    fun readCsv(csvPath: String): MutableSet<Tunnel> {
        val importedTunnels: HashSet<Tunnel> = HashSet()

        val br = BufferedReader(FileReader(csvPath))
        // Get header
        var line: String = br.readLine()
        val colNamesArr: Array<String> = line.split(COMMA_DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val colNames: List<String> = listOf(*colNamesArr)
        if (colNames == tunnelConfHeaders) {
            while ((br.readLine().also { line = it }) != null) {
                val values: Array<String> = line.split(COMMA_DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (values.size == tunnelConfHeaders.size) {
                    val tunnel = Tunnel()
                    tunnel.localAddress = values[0]
                    tunnel.localPort = values[1].toInt()
                    tunnel.remoteAddress = values[2]
                    tunnel.remotePort = values[3].toInt()
                    tunnel.local = values[4].equals("local", ignoreCase = true)
                    importedTunnels.add(tunnel)
                }
            }
        }
        return importedTunnels
    }

    private fun convertToCSV(data: Array<String?>): String {
        return Stream.of(*data)
            .map { values: String? -> values?.let{this.escapeSpecialCharacters(values)} }
            .collect(Collectors.joining(","))
    }

    private fun escapeSpecialCharacters(data: String): String {
        var escapedData = data.replace("\\R".toRegex(), " ")
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            val values: String = data.replace("\"", "\"\"")
            escapedData = "\"" + values + "\""
        }
        return escapedData
    }

    @Throws(FileNotFoundException::class)
    fun writeCsv(tunnels: List<Tunnel>, csvPath: String) {
        val dataLines: MutableList<Array<String?>> = ArrayList()
        // Add header
        dataLines.add(tunnelConfHeaders.toTypedArray<String?>())

        for (tunnel in tunnels) {
            val line = arrayOfNulls<String>(tunnelConfHeaders.size)
            line[0] = tunnel.localAddress
            line[1] = tunnel.localPort.toString()
            line[2] = tunnel.remoteAddress
            line[3] = tunnel.remotePort.toString()
            if (tunnel.local) line[4] = "local"
            else line[4] = "remote"
            dataLines.add(line)
        }
        PrintWriter(csvPath).use { pw ->
            dataLines.stream().map { data: Array<String?> -> this.convertToCSV(data) }
                .forEach { x: String? -> pw.println(x) }
        }
    }

    companion object {
        const val COMMA_DELIMITER: String = ","
    }
}
