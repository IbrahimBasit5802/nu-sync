package com.example.nusync;

import com.example.nusync.config.Config;
import com.example.nusync.data.TeacherAllocation;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.ArrayList;
import java.util.List;
import com.google.api.services.sheets.v4.model.*;


public class SheetsAPIFetcher {

    private static final String APPLICATION_NAME = "NU Sync Desktop App";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    // This method fetches both the data and the formatting.
    public Sheet fetchDataAndFormat(String sheetName, String spreadsheetId) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId)
                .setRanges(List.of(sheetName))
                .setIncludeGridData(true)
                .setKey(Config.API_KEY)
                .execute();

        return spreadsheet.getSheets().get(0); // We assume there's only one sheet in the response.
    }

    public List<List<Object>> fetchSheetData(String range, String spreadsheetId) throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .setKey(Config.API_KEY)
                .execute();

        return response.getValues();
    }



}

