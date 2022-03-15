package com.tinyelectronicblog.remotecontrol;

/**
 * MIT License
 * Copyright (c) 2022 TinyElectronicBlog
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.values.strings.MultilingualStrings;

public class Strings {

    public static final MultilingualStrings settings_disconnect = new MultilingualStrings("DISCONNETTITI","LOG OUT");
    public static final MultilingualStrings settings_exit = new MultilingualStrings("CHIUDI APPLICAZIONE","CLOSE APPLICATION");
    public static final MultilingualStrings settings_authenticated_as = new MultilingualStrings("Autenticato come:","Authenticated as:");
    public static final MultilingualStrings settings_workspace_folder = new MultilingualStrings("Inserire la cartella di workspace","Enter workspace folder");
    public static final MultilingualStrings settings_id = new MultilingualStrings("Inserisci l'ID del telecomando","Enter remote control ID");
    public static final MultilingualStrings settings_update_interval = new MultilingualStrings("Aggiornamento dati in secondi","Data update in seconds");
    public static final MultilingualStrings settings_languages = new MultilingualStrings("Seleziona una lingua:","Select a language:");
    public static final MultilingualStrings settings_set_values = new MultilingualStrings("IMPOSTA VALORI","SET VALUES");
    public static final MultilingualStrings settings_set_values_toast1 = new MultilingualStrings("Nome della cartella di workspace errato","Wrong workspace folder name");
    public static final MultilingualStrings settings_set_values_toast2 = new MultilingualStrings("Errato valore dell'ID del telecomando","Wrong remote control ID value");
    public static final MultilingualStrings settings_set_values_toast3 = new MultilingualStrings("Cartella di workspace, ID del telecomando e intervallo aggiornamento dati salvati","Workspace folder, remote control ID and data update interval saved");
    public static final MultilingualStrings settings_set_values_toast5 = new MultilingualStrings("Attenzione, non è stato possibile salvare le impostazioni su file", "Warning, settings not saved on file");
    public static final MultilingualStrings settings_set_language = new MultilingualStrings("Imposta lingua", "Set language");
    public static final MultilingualStrings settings_set_language_toast = new MultilingualStrings("Lingua salvata","Language saved");
    public static final MultilingualStrings settings_drive_error1 = new MultilingualStrings("La ricerca della cartella di Tiny Electronic Blog su Google Drive è fallita","The search for the Tiny Electronic Blog folder on Google Drive failed");
    public static final MultilingualStrings settings_drive_error2 = new MultilingualStrings("Nella root di Google Drive può esserci una sola cartella di Tiny Electronic Blog, rimuovere le cartelle in eccesso", "In the root of Google Drive there can be only one folder of Tiny Electronic Blog, remove the excess folders");
    public static final MultilingualStrings settings_drive_error3 = new MultilingualStrings("Creazione della cartella di Tiny Electronic Blog su Google Drive fallita", "Tiny Electronic Blog folder creation on Google Drive failed");
    public static final MultilingualStrings settings_drive_error4 = new MultilingualStrings("La ricerca della cartella di workspace su Google Drive è fallita","The search for the workspace folder on Google Drive failed");
    public static final MultilingualStrings settings_drive_error5 = new MultilingualStrings("Su Google Drive può esserci una sola cartella di workspace, rimuovere le cartelle in eccesso", "There can be only one workspace folder on Google Drive, remove the excess folders");
    public static final MultilingualStrings settings_drive_error6 = new MultilingualStrings("Creazione della cartella di workspace su Google Drive fallita", "Workspace folder creation on Google Drive failed");
    public static final MultilingualStrings settings_drive_error7 = new MultilingualStrings("La ricerca del file di configurazione del remote control su Google Drive è fallita","The search for the remote control configuration file on Google Drive failed");
    public static final MultilingualStrings settings_drive_error8 = new MultilingualStrings("Su Google Drive può esserci al massimo un solo file di configurazione del remote control, rimuovere i file in eccesso", "There can be only one remote control configuration file on Google Drive, remove the excess files");
    public static final MultilingualStrings settings_drive_error9 = new MultilingualStrings("Creazione del file di configurazione del remote control su Google Drive fallita", "Remote control configuration file creation on Google Drive failed");
    public static final MultilingualStrings settings_drive_loading = new MultilingualStrings("Google Drive in caricamento...","Google Drive loading ...");
    public static final MultilingualStrings settings_drive_synchronized = new MultilingualStrings("Drive sincronizzato","Drive synchronized");
    public static final MultilingualStrings settings_reset_workspace = new MultilingualStrings("Resetta workspace","Reset workspace");

    public static final MultilingualStrings on_off_enter_value = new MultilingualStrings("Inserisci valore","Enter value");
    public static final MultilingualStrings on_off_send_value = new MultilingualStrings("Invia valore","Send value");
    public static final MultilingualStrings on_off_send_command = new MultilingualStrings("Invio comando...","Sending command...");
    public static final MultilingualStrings on_off_null_value = new MultilingualStrings("Non puoi inviare un comando con valore nullo","You cannot send a command with null value");
    public static final MultilingualStrings on_off_send_failed = new MultilingualStrings("Invio fallito :(","Sending failed :(");
    public static final MultilingualStrings on_off_command_sent = new MultilingualStrings("Invio riuscito :)","Sending successful :)");
    public static final MultilingualStrings on_off_more_command = new MultilingualStrings("Non puoi inviare più di un comando contemporaneamente","You cannot send more than one command at the same time");
    public static final MultilingualStrings on_off_ready = new MultilingualStrings("Pronto!","Ready!");

    public static final MultilingualStrings dashboard_unreadable_value = new MultilingualStrings("Valore illeggibile","Unreadable value");
    public static final MultilingualStrings dashboard_no_device = new MultilingualStrings("Nessun dispositivo rilevato","No device detected");

    public static final MultilingualStrings remotecontrol_drive_failed = new MultilingualStrings("Connessione a Google Drive persa; se il problema persiste, premere IMPOSTA VALORI nella schermata delle impostazioni per provare a ricollegarsi.","Google Drive connection lost; if the problem persists, press SET VALUES on the settings screen to try to reconnect.");

}