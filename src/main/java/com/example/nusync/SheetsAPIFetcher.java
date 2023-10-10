package com.example.nusync;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.List;
import com.google.api.services.sheets.v4.model.*;


public class SheetsAPIFetcher {

    private static final String APPLICATION_NAME = "NU Sync Desktop App";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final String apiKey;
    private final String spreadsheetId;

    public SheetsAPIFetcher(String apiKey, String spreadsheetId) {
        this.apiKey = "AIzaSyAIUw5fOxesfapuLhn8r11lI6_TXKXuwvY";
        this.spreadsheetId = "1knS7NRf3WjqFOnd-b5NTx1rvWNqvNK5jjecY0fkhcXM";
    }

    // This method fetches both the data and the formatting.
    public Sheet fetchDataAndFormat(String sheetName) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId)
                .setRanges(List.of(sheetName))
                .setIncludeGridData(true)
                .setKey(apiKey)
                .execute();

        return spreadsheet.getSheets().get(0); // We assume there's only one sheet in the response.
    }

    public List<List<Object>> fetchSheetData(String range) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .setKey(apiKey)
                .execute();

        return response.getValues();
    }
}

