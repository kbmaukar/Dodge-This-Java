package com.dodgethis.main;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.Random;

public class Game extends Canvas implements Runnable {

    private static final long serialVersionUID = -8881108351757493300L;

    public static final int WIDTH = 640, HEIGHT = WIDTH / 12 * 9;
    private Thread thread; //To initiate single-threading
    private boolean running = false;

    private Random r;
    private Handler handler;
    private HUD hud;
    private Spawn spawner;

    public Game(){
        //Note: keep track of the order of methods/initialisations
        handler = new Handler();
        this.addKeyListener(new KeyInput(handler)); //Listens for key input

        new Window(WIDTH, HEIGHT, "Dodge This!", this);

        hud = new HUD();
        spawner = new Spawn(handler, hud);
        r = new Random(); //Randomizer for testing purposes

        handler.addObject(new Player(WIDTH/2-32, HEIGHT/2-32, ID.Player, handler)); //Spawns player at the centre for now
        // for(int i = 0; i < 10; i++) Sample loop to generate set number of game objects
        // handler.addObject(new BossEnemy( (Game.WIDTH/2) - 48, -156, ID.BossEnemy, handler)); //BossEnemy is spawned off-screen
        handler.addObject(new BasicEnemy(r.nextInt(Game.WIDTH), r.nextInt(Game.HEIGHT), ID.BasicEnemy, handler));
        handler.addObject(new BasicEnemy(r.nextInt(Game.WIDTH), r.nextInt(Game.HEIGHT), ID.BasicEnemy, handler));

    }

    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        this.requestFocus(); //Auto focus on game initialization by default
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1){
                tick();
                delta--;
            }
            if(running)
                render();
            frames++;

            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                //System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
        stop();
    } // this is a functional game loop template ripped off some place... just lots of math

    private void tick(){
        handler.tick();
        hud.tick();
        spawner.tick();
    }

    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        handler.render(g);

        hud.render(g);

        g.dispose();
        bs.show();
    }

    public static float clamp(float var, float min, float max){
        if(var >= max)
            return var = max;
        else if(var <= min)
            return var = min;
        else
            return var;
    } //Method to set game object behavior when it reaches specified bounds

    public static void main(String args[]){
        new Game();
    }
}