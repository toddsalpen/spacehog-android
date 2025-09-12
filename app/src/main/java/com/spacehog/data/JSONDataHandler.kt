package com.spacehog.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import java.io.FileNotFoundException
import java.io.IOException

class JSONDataHandler(var context: Context) {

    /**
     * Checks if a file exists in the app's internal storage.
     */
    fun isFileExist(fileName: String): Boolean {
        return context.getFileStreamPath(fileName).exists()
    }

    /**
     * Deletes a file from the app's internal storage.
     * @return true if the file was deleted successfully or did not exist.
     */
    fun deleteFile(fileName: String): Boolean {
        val file = context.getFileStreamPath(fileName)
        // The file is gone if it didn't exist to begin with, or if delete() succeeds.
        return !file.exists() || file.delete()
    }

    /**
     * Writes a JSONArray to a file in the app's internal storage.
     * This will overwrite the file if it already exists.
     */
    fun writeToFile(array: JSONArray, fileName: String) {
        try {
            // The .writer() and .use extensions handle closing the stream automatically.
            context.openFileOutput(fileName, Context.MODE_PRIVATE).writer().use {
                it.write(array.toString())
            }
        } catch (e: IOException) {
            Log.e("JSONDataHandler", "File write failed for $fileName", e)
        }
    }

    /**
     * Reads a JSONArray from a file in the app's internal storage.
     * @return The parsed JSONArray, or an empty JSONArray if the file is not found,
     * cannot be read, or contains invalid JSON.
     */
    fun readFromFile(fileName: String): JSONArray {
        // The return value of the 'try' expression is used directly.
        return try {
            // .bufferedReader() and .use { it.readText() } is the modern way to read a file's content.
            val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            JSONArray(jsonString)
        } catch (e: FileNotFoundException) {
            // It's fine if the file doesn't exist yet, just return an empty array.
            Log.i("JSONDataHandler", "File not found, creating new data: $fileName")
            JSONArray()
        } catch (e: IOException) {
            Log.e("JSONDataHandler", "Cannot read file: $fileName", e)
            JSONArray() // Return empty on read error
        } catch (e: JSONException) {
            Log.e("JSONDataHandler", "Error parsing JSON from file: $fileName", e)
            JSONArray() // Return empty on parsing error
        }
    }
}