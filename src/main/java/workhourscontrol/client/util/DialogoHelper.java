package workhourscontrol.client.util;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

public class DialogoHelper {

	/**
	 * Exibe di�logo onde com op�oes Sim, N�o e Cancelar para usu�rio confirmar se deseja fechar aplica��o
	 * Recebe como par�metro a a��o a ser realizada.
	 */
	public static boolean confirmarFechamentoAplicacao(Runnable action) {
		Action response = Dialogs.create()
				.title("Aten��o")
				.masthead("Existem altera��es n�o salvas.")
				.message("Deseja salvar as altera��es?")
				.showConfirm();

		if (response == Dialog.Actions.YES) {
			action.run();
			return true;
		} else if (response == Dialog.Actions.NO) {
			return true;
		} else {
			return false;
		}

	}
}
