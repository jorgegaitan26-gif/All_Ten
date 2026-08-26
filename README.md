# Lista de comandos

## Configuración local inicial

` instalar git: winget install --id Git.Git -e `

`git config --global user.name "Jorge gaitan"` : Define tu nombre global para firmar commits en tu máquina.

`git config --global user.email "jelgaitan@poligran.edu.co"` : Define tu email global para firmar commits en tu máquina.

## Configuración de repositorio local

`git init` : Inicializa un repositorio de git en la carpeta actual.

`git remote add origin [URL-GITHUB]`: Conecta tu repositorio local con tu repositorio de GitHub.

## Interacción con remote (GitHub)

`git clone` : Descarga un repositorio remoto a tu máquina local.

`git pull` : Trae cambios del repositorio remoto.

`git pull --rebase` : Trae los cambios del repositorio remoto y “adelanta” tus commits locales sobre los commits remotos, manteniendo un historial lineal.

`git pull --merge` : Trae los cambios del repositorio remoto y crea un merge commit para combinar los cambios remotos con los locales si hay divergencia.

`git push` : Sube tus commits locales al repositorio remoto.

`git push -u origin [nombre rama]` : Sube una nueva rama al remoto y la deja vinculada con la rama actual para futuros push/pull automáticos.

## Creación de commit

`git status` : Nos indica el estado actual de los archivos que podemos agregar o commitear.

`git add .` : Agrega todos los cambios del directorio actual al área de staging (zona intermedia donde se almacena todo lo que va a entrar en el siguiente commit).

`git commit -m "cambios del commit"` : Crea un commit con los cambios agregados y el mensaje especificado.

## Historial de commits

`git log` : Muestra el historial completo de commits con detalles. (Si es muy largo, usa las flechas para navegar y “Q” para salir)

`git log --oneline` : Muestra el historial resumido en una línea por commit.

## Manejo de ramas

`git checkout` : Cambia tu posición a otra rama o commit.

`git checkout -b [nombre nueva rama]` : Crea una nueva rama y se mueve a ella.

`git merge [rama a mergear]` : Fusiona la rama indicada con la rama actual.

`git branch -d [nombre rama]` : Elimina la rama seleccionada.

## Manejo rapido y subida de Commit

`git add .`

`git commit -m "descripción de los cambios"`

`git push`

## git add "READMEN.md" en caso de que el archivo fue modificado después del último commit. para evitar error "M"

## Investigacion: navegacion entre vistas en JavaFX

### Contexto de ALL TEN

ALL TEN sera un juego local desarrollado con JavaFX. Aunque no es una pagina web, puede organizarse de forma parecida: cada vista representa una pantalla y los botones permiten navegar entre ellas.

### Vistas principales

El juego puede comenzar con estas pantallas:

```text
Menu principal -> Pantalla de juego -> Resultados
	   ^                                  |
	   +----------------------------------+
```

- **Menu principal:** inicia el juego.
- **Pantalla de juego:** contiene la logica y los controles principales.
- **Resultados:** muestra el resultado y permite volver al menu.

### Conceptos basicos

- **Stage:** ventana principal de la aplicacion.
- **Scene:** escena que se muestra dentro de la ventana.
- **Node:** componente visual, como un boton, una etiqueta o un contenedor.
- **Navegacion:** cambio de la escena o del contenido que se muestra en la ventana.

### Ejemplo minimo de navegacion

```java
private Stage ventana;

private void mostrarMenuPrincipal() {
	Label titulo = new Label("ALL TEN");
	Button jugar = new Button("Jugar");

	jugar.setOnAction(event -> mostrarPantallaJuego());

	VBox layout = new VBox(15, titulo, jugar);
	ventana.setScene(new Scene(layout, 600, 400));
}

private void mostrarPantallaJuego() {
	Label titulo = new Label("Partida en curso");
	Button volver = new Button("Volver al menu");

	volver.setOnAction(event -> mostrarMenuPrincipal());

	VBox layout = new VBox(15, titulo, volver);
	ventana.setScene(new Scene(layout, 600, 400));
}
```

En este ejemplo, `setOnAction` detecta el clic del usuario y `setScene` cambia la vista visible. Para un proyecto pequeño, este enfoque es suficiente y facil de mantener.

### Estructura recomendada

```text
src/
└── allten/
	├── AllTenApp.java
	├── MenuView.java
	├── JuegoView.java
	└── ResultadosView.java
```

