package workhourscontrol.client.util;

import java.io.File;
import java.util.prefs.Preferences;

import workhourscontrol.client.MainApp;

public class PreferencesHelper {

	 /**
     * Retorna o arquivo de prefer�ncias da pessoa, o �ltimo arquivo que foi aberto.
     * As prefer�ncias s�o lidas do registro espec�fico do SO (Sistema Operacional).
     * Se tais pref�rencias n�o puderem  ser encontradas, ele retorna null.
     */
	public static File getEnderecoArquivo(String chave) {
		String filePath = getPref(chave);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
	}

	 /**
     * Define o caminho do arquivo carregado atual. O caminho � persistido no
     * registro espec�fico do SO (Sistema Operacional).
     *
     * @param file O arquivo ou null para remover o caminho
     */
    public static void setPersonFilePath(String chave, File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put(chave, file.getPath());
        } else {
            prefs.remove(chave);
        }
    }

    public static String getPref(String chave) {
    	Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
    	return prefs.get(chave, null);
    }

    public static void setPref(String chave, String valor) {
    	Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
    	prefs.put(chave, valor);
    }
}
