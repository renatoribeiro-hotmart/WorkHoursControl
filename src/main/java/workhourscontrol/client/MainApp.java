package workhourscontrol.client;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.controlsfx.dialog.Dialogs;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import workhourscontrol.client.controller.RootLayoutController;
import workhourscontrol.client.controller.WorkHoursManagerController;
import workhourscontrol.client.model.RegistroHoraObservable;
import workhourscontrol.client.service.XmlService;
import workhourscontrol.client.util.DialogoHelper;
import workhourscontrol.client.util.FileHelper;
import workhourscontrol.client.util.PreferencesHelper;

public class MainApp extends Application{
	private Logger logger = Logger.getLogger(MainApp.class);

	public static final String NOME_PREFERENCIA_XML = "xmlPath";
	private static final String PROPERTY_FILE = "propriedades.xml";

	public static ConfiguracoesAplicacao configuracoesAplicacao = new ConfiguracoesAplicacao();

	private Stage primaryStage;
    private BorderPane rootLayout;
	private ObservableList<RegistroHoraObservable> registrosHora;
	private File arquivoAberto;

	// Atributos para verificas se registros j� foram salvos
	private BooleanProperty saved = new SimpleBooleanProperty();
	private InvalidationListener savedListener;

	private XmlService xmlService;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
    	initServices();
    	initConfig();
        initStage(primaryStage);
        initCollection();
        initRootLayout();
        initWorkHoursManager();
    }

	private void initStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Controle de Horas Trabalhadas");

        primaryStage.setOnCloseRequest(evt -> {
        	final boolean isSaved = saved.get();
			if (!isSaved && !confirmarFechamentoAplicacao()) {
        		evt.consume();
        	}
        });

	}

	/**
	 * Usu�rio confirma se deve salvar o estado da aplica��o no arquivo.
	 */
	private boolean confirmarFechamentoAplicacao() {
		return DialogoHelper.confirmarFechamentoAplicacao(() -> salvar());
	}

	private void initCollection() {
		registrosHora = FXCollections.observableArrayList();
		savedListener = obs -> setNotSaved();
		registrosHora.addListener(savedListener);
	}

	private void initConfig() {
		try {
			File arquivoPropriedades = new File(getPropertyFileName());

			if (arquivoPropriedades.exists()) {
				logger.info("Utilizando arquivo " + arquivoPropriedades.getAbsolutePath());
				configuracoesAplicacao = xmlService.carregarXml(arquivoPropriedades, ConfiguracoesAplicacao.class);
			} else {
				logger.warn("O arquivo de configura��es '" + getPropertyFileName() + "' n�o foi encontrado ");
			}
		} catch(Exception e) {
			logger.error("Ocorreu um erro ao carregar arquivo xml de configura��es", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Primeiro verifica se o nome do arquivo de propriedades foi passado como par�metro para a vm
	 */
	private String getPropertyFileName() {
		final String propertyParameter = System.getProperty("arquivoPropriedades");
		return propertyParameter != null ? propertyParameter : PROPERTY_FILE;
	}

	private void initServices() {
		this.xmlService = XmlService.getInstance();
	}

	/**
     * Inicializa o root layout e tenta carregar o �ltimo arquivo
     * de pessoa aberto.
     */
    public void initRootLayout() {
        try {
            // Carrega o root layout do arquivo fxml.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
                    .getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Adiciona referência à MainApp
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            // Mostra a scene (cena) contendo o root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            primaryStage.show();

        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void initWorkHoursManager() {
    	  try {
              // Carrega a tela principal do gerenciamento de horas.
              FXMLLoader loader = new FXMLLoader();
              loader.setLocation(MainApp.class.getResource("view/WorkHoursManager.fxml"));
              AnchorPane workHoursManager = (AnchorPane) loader.load();

              // Adiciona referência à MainApp
              WorkHoursManagerController controller = loader.getController();
              controller.setMainApp(this);

              // Define o person overview dentro do root layout.
              rootLayout.setCenter(workHoursManager);

              // Se algum arquivo foi aberto recentemente, carrega o mesmo
              carregarXmlPreferencias();

          } catch (IOException e) {
        	  logger.error(e);
          }

    }

    /**
     * Carrega ultimo xml aberto, se houver
     */
	private void carregarXmlPreferencias() {
		try {
			arquivoAberto = PreferencesHelper.getEnderecoArquivo(NOME_PREFERENCIA_XML);
			if (Objects.nonNull(arquivoAberto)) {
				carregarRegistrosDoArquivo(arquivoAberto);
			}
		} catch (Exception e) {
			logger.warn("N�o foi poss�vel carregar �ltimo arquivo xml aberto.", e);
		}
	}

    public ObservableList<RegistroHoraObservable> getRegistrosHora() {
    	return this.registrosHora;
    }


    public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
     * Salva lista de registros em arquivo xml
     */
    public void salvarRegistrosNoArquivo(File file) {
    	try {
			xmlService.salvarRegistroHoraXml(file, registrosHora);
			saved.set(true);
		} catch (Exception e) {
			logger.error("Ocorreu um erro ao salvar arquivo xml", e);
			throw new RuntimeException(e);
		}
    }

    /**
     * Carrega registros do arquivo xml
     * @throws Exception
     */
    public void carregarRegistrosDoArquivo(File file) throws Exception {
		registrosHora.addAll(xmlService.carregarRegistroHoraXml(file));
		saved.set(true);
    }

	public void limparRegistros() {
		this.registrosHora.clear();
	}

	public File getArquivoAberto() {
		return arquivoAberto;
	}

	public void setArquivoAberto(File arquivoAberto) {
		this.arquivoAberto = arquivoAberto;
		PreferencesHelper.setPersonFilePath(MainApp.NOME_PREFERENCIA_XML, arquivoAberto);
	}

	public File getDiretorioArquivoAberto() {
		if (Objects.nonNull(arquivoAberto)) {
			return arquivoAberto.getParentFile();
		}
		return null;
	}

	public void setNotSaved() {
		saved.set(false);
	}

	public void fecharAplicacao() {
		primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	public void salvarComo() {
		File arquivo = FileHelper.chooseFileForSaving(primaryStage, "*.xml", "XML files (*.xml)", getDiretorioArquivoAberto());
		if (Objects.nonNull(arquivo)) {
			salvarRegistrosNoArquivo(arquivo);
			setArquivoAberto(arquivo);
		}
	}

	public void carregar() {
		try {
			File arquivo = FileHelper.chooseFileForOpening(getPrimaryStage(), "*.xml", "XML files (*.xml)", getDiretorioArquivoAberto());
			if (Objects.nonNull(arquivo)) {
				limparRegistros();
				carregarRegistrosDoArquivo(arquivo);
				setArquivoAberto(arquivo);
			}
		} catch (Exception e) {
			logger.error("Ocorreu um erro ao carregar arquivo xml", e);
		}
	}

	public void novo() {
		limparRegistros();
		setArquivoAberto(null);
	}

	public void salvar() {
		File arquivo = getArquivoAberto();
		if (Objects.nonNull(arquivo)) {
			salvarRegistrosNoArquivo(arquivo);
		} else {
			salvarComo();
		}
	}

	public void showAppVersion() {

		try {
			Properties prop = new Properties();
			prop.load(getClass().getResourceAsStream("/application.properties"));

			Dialogs.create()
			.title("About")
			.message("Vers�o: " + prop.getProperty("application.version"))
			.showInformation();

		} catch (IOException e) {
			logger.error(e);
		}

	}
}
