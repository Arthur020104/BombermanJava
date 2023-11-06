package com.mygdx.game.fase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.mygdx.game.Bomberman;
import com.badlogic.gdx.Input.Keys;

/*"implements" é usado para garantir que a classe MainGameScreen cumpra todos os métodos 
 * definidos na interface Screen. Isso significa que a classe deve fornecer implementações para todos 
 * os métodos declarados na interface*/
public class MainGame implements Screen 
{
    private BitmapFont font;
    private OrthographicCamera font_cam;
    private Player[] players;
    private Camada[] camadas;
    private Bomberman game;
    private boolean gameOver;
    private Texture spriteDeCoracaoTexture;

    public MainGame(Bomberman game) 
    {
        // Inicialização
        spriteDeCoracaoTexture = new Texture("spriteDeCoracao.png");
        camadas = new Camada[5];
        gameOver = false;
        this.game = game;
        font = new BitmapFont(Gdx.files.internal("fontLucidaSans.fnt"));
        this.generateCamadaTextures();
		players = new Player[2];
		players[0] = new Player(0,0, "player1", 3,
				Keys.UP, Keys.RIGHT, Keys.DOWN, Keys.LEFT, Keys.SHIFT_RIGHT);
		players[1] = new Player(camadas[3].getGridSnap() - 2, camadas[3].getGridSnap()-1, "player2", 3,
			Keys.W, Keys.D, Keys.S, Keys.A, Keys.F);
    }

    @Override
    public void show() 
    {
        // Configuração da câmera
        font_cam = new OrthographicCamera();
        font_cam.setToOrtho(false, Bomberman.WIDTH, Bomberman.HEIGHT);
    }

    public void render(float delta) 
    {
        // Limpa o buffer de tela
        Gdx.gl.glClearColor(0.6f, 0.4f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Verifica se o jogo acabou
        gameOver = (!players[0].taVivo() || !players[1].taVivo());

        if (!gameOver) 
        {
            for (int i = 0; i < players.length; i++) 
            {
                int[] playersPos = players[i].getCurrentPos();
                Boolean[] posOcupadas = camadas[3].posAdjOcupadas(players[i]);

                int pos = players[i].handleInput(Gdx.input, camadas[3].getGridSnap(), posOcupadas);

                camadas[3].verificaBombasNaCamada(players[i], delta);
                camadas[3].explosaoManager(delta);

                // Remove o jogador da posição anterior e o coloca na posição atual
                camadas[3].setObjetoDoJogo(null, playersPos[0], playersPos[1]);
                playersPos = players[i].getCurrentPos();
                camadas[3].setObjetoDoJogo(players[i], playersPos[0], playersPos[1]);

                // Gerencia colisões
                camadas[3].manejaColisao(pos, players[i].getAdjacentPositions(camadas[3].getGridSnap()), players, players[i]);

                game.batch.begin();

                // Renderiza as camadas do jogo
                this.renderizaCamadasdoGame();

                game.batch.end();
            }
        } 
        else 
        {
            // Volta para o menu principal quando o jogo acaba
            game.setScreen(new MainMenu(game));
        }
    }
	    /**
	     * Renderiza as camadas do jogo, incluindo objetos, jogadores e informações de vida.
	     */
	    public void renderizaCamadasdoGame() 
	    {
	        // Itera sobre as camadas do jogo
	        for (int camada = 0; camada < camadas.length; camada++) 
	        {
	            // Itera sobre as posições na camada atual
	            for (int h = 0; h < camadas[camada].getGridSnap(); h++) 
	            {
	                for (int j = 0; j < camadas[camada].getGridSnap(); j++) 
	                {
	                    // Obtém o objeto na posição atual
	                    ObjetoDoJogo objeto = camadas[camada].getObjetoDoJogo(h, j);
	                    if (objeto != null) 
	                    {
	                        if (camada != 1) 
	                        {
	                            // Renderiza o objeto na camada, ajustando a posição e o tamanho conforme necessário
	                            game.batch.draw(objeto.geTexture(), h * camadas[camada].getImageSize(),
	                                    j * camadas[camada].getImageSize(), camadas[camada].getImageSize(),
	                                    camadas[camada].getImageSize());
	                        } 
	                        else 
	                        {
	                        	// Adiciona um deslocamento aleatório para a camada 1 para que os assets de detalhes sejam renderizados de forma mais variada
	                        	long seed = (long) h * 2654435761L + (long) j * 2654435789L;
	                        	Random random = new Random(seed);
	                        	float offsetX = random.nextFloat() * camadas[camada].getImageSize();
	                        	float offsetY = random.nextFloat() * camadas[camada].getImageSize();
	                        	// Renderiza o objeto na camada 1 com o deslocamento aleatório
	                        	game.batch.draw(objeto.geTexture(), h * camadas[camada].getImageSize() + offsetX,
	                        	        j * camadas[camada].getImageSize() + offsetY,
	                        	        camadas[camada].getImageSize() / 4, camadas[camada].getImageSize() / 4);
	                        }
	                    }
	                }
	            }
	        }

	        // Define a projeção da câmera para a fonte utilizada para renderizar informações de vida
	        game.batch.setProjectionMatrix(font_cam.combined);
	        
	        // Itera sobre os jogadores para renderizar as informações de vida
	        for (int i = 0; i < players.length; i++) 
	        {
	            String text = "Player " + (i + 1) + "  ";

	            // Calcula a largura do texto para posicionar corretamente as informações de vida
	            GlyphLayout layout = new GlyphLayout();
	            layout.setText(font, text);
	            float textWidth = layout.width;

	            float offset = 0f;
	            // Ajusta o deslocamento para o segundo jogador para alinhar as informações à direita da tela
	            if (i == 1) 
	            {
	                offset = Bomberman.WIDTH - (players[i].getVida() * (3.5f * spriteDeCoracaoTexture.getWidth()) + textWidth);
	            }
	            
	            // Renderiza os corações indicando a vida do jogador
	            int j;
	            for (j = 0; j < players[i].getVida(); j++) 
	            {
	                game.batch.draw(spriteDeCoracaoTexture,
	                        offset + textWidth + (j * (3.5f * spriteDeCoracaoTexture.getWidth())),
                           Bomberman.HEIGHT - (spriteDeCoracaoTexture.getHeight() * 3.5f),
	                        (3.5f * spriteDeCoracaoTexture.getWidth()), spriteDeCoracaoTexture.getHeight() * 3.5f);
	            }
	            // Renderiza o texto indicando o jogador e a posição dos corações
	            font.draw(game.batch, layout, offset, Bomberman.HEIGHT - 10);
	            // Atualiza o deslocamento para o próximo jogador
	            offset += textWidth + (j * (3.5f * spriteDeCoracaoTexture.getWidth()));
	        }
	    }

	public void generateCamadaTextures() 
	{
	    int camada_size=10;
		for (int i = 0; i < camadas.length; i++)
	    {
	        camadas[i] = new Camada(camada_size);
	    }
	    // Camada de detalhes do mapa PLACEHOLDER
	    camadas[1].generateDetailTextures();

	    // Gerando chão (tile de grama)
	    camadas[0].generateGroundTextures();

	    // Gerando areia abaixo do tile quebrável
	    camadas[0].generateSandTextures();

	    // Gerando tile quebraveis
		int[][] matrix;
		try {
			matrix = importLayout(camada_size, null);
		}catch(IOException e) {
			e.printStackTrace();
			matrix = randomLayout(camada_size);
		}

		camadas[3].setBlockLayout(matrix);

	}

	public int[][] importLayout(int size, String filepath) throws IOException{
		int[][] matrix = new int[size][size];
		if(filepath != null) {
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			String line;
			int row = 0;
			while ((line = reader.readLine()) != null && row < size) {
				for (int col = 0; col < Math.min(size, line.length()); col++) {
					char c = line.charAt(col);
					if (Character.isDigit(c)) {
						matrix[row][col] = Character.getNumericValue(c);
					} else {
						matrix[row][col] = 0;
					}
				}
				row++;
			}
				reader.close();
				matrix[0][0] = 0;
				matrix[size-1][size-1] = 0;
				return matrix;
		}
		return randomLayout(size);
	}

	public int[][] randomLayout(int size){
		int matrix[][] = new int[size][size];
		for(int i=0; i<size;i++) {
			for(int j=0; j<size;j++) {
				if((i==0 && j<3) ||
					(i<3 && j==0) ||
					(i==size-1 && j>size-4) ||
					(i>size-4 && j==size-1)){
					matrix[i][j] = 0;
					continue;
				}
				Random random = new Random();
				int chance = random.nextInt(100)+1;
				if(chance<=45) matrix[i][j] = 1;
				else if(chance<=55) matrix[i][j] = 2;
				else if(chance<=65) matrix[i][j] = 3;
				else matrix[i][j] = 0;
			}
		}
		matrix[0][0] = 0;
		matrix[size-1][size-1] = 0;
		return matrix;
	}

	public static void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int digit : row) {
				System.out.print(digit + " ");
			}
			System.out.println();
		}
	}


@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
