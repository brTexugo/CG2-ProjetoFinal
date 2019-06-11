package mygame;


import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;

import com.jme3.bullet.util.CollisionShapeFactory;

import com.jme3.input.KeyInput;

import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;

import com.jme3.math.ColorRGBA;

import com.jme3.scene.Spatial;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;

/*
    140841 - Gabriel Sant'Ana Vieira
    132204 - Rafael Rodrigues Banhos
 */
public class Main extends SimpleApplication
        implements ActionListener {
  private AudioNode audio_gun;  
  private AudioNode audio_nature; 
  private Spatial sceneModel;
  private BulletAppState bulletAppState;
  private RigidBodyControl landscape;
  private CharacterControl player;
  private Vector3f walkDirection = new Vector3f();
  private boolean left = false, right = false, up = false, down = false;

  private double vida = 100;
      
  private float elefVelocidade = 5f;
  
  private float ultimoTempo = 0f;
  private float dificuldadeT = 0f;
  private final List<Vector3f> pElef = new ArrayList();
  private int qtdElef = 0;
  
  private Vector3f camDir = new Vector3f();
  private Vector3f camLeft = new Vector3f();
  private Node elefante;
  Node player1;
  
  Material wall_mat;
  Material stone_mat;
  Material floor_mat;

  private RigidBodyControl    brick_phy;
  private RigidBodyControl    ball_phy;
  private static final Sphere sphere;
  private RigidBodyControl    floor_phy;
 
  static {
    /** Initialize the cannon ball geometry */
    sphere = new Sphere(32, 32, 0.4f, true, false);
    sphere.setTextureMode(TextureMode.Projected);
     
  }


  public static void main(String[] args) {
    Main app = new Main();
    app.start();
  }

  public void simpleInitApp() {
     
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);

    viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
    flyCam.setMoveSpeed(100);
    setUpKeys();
    setUpLight();

    sceneModel = assetManager.loadModel("Models/town/main.scene");
    sceneModel.setLocalScale(2f);

    CollisionShape sceneShape =
            CollisionShapeFactory.createMeshShape(sceneModel);
    landscape = new RigidBodyControl(sceneShape, 0);
    sceneModel.addControl(landscape);

    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);

    player.setGravity(new Vector3f(0,-30f,0));

    player.setPhysicsLocation(new Vector3f(0, 10, 0));

    inputManager.addMapping("shoot",
        new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addListener(actionListener, "shoot");
   
    initMaterials();
    initAudio();
    initCrossHairs();
    
    elefante = new Node("Elefantes");
    rootNode.attachChild(sceneModel);
    
    bulletAppState.getPhysicsSpace().add(landscape);
    bulletAppState.getPhysicsSpace().add(player);
    
    rootNode.attachChild(elefante);
    
    rootNode.attachChild(sceneModel);
    
    pElef.add(new Vector3f(-80f, 1f, -30f));
    pElef.add(new Vector3f(6f, 1f, 79f));
    pElef.add(new Vector3f(60f, 1f, 85f));
    pElef.add(new Vector3f(-3f, 1f, -150));
    pElef.add(new Vector3f(40f, 1f, -140f));
    pElef.add(new Vector3f(80f, 1f, -100f));
    
    bulletAppState.getPhysicsSpace().add(landscape);
    bulletAppState.getPhysicsSpace().add(player);
  }
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("shoot") && !keyPressed) {
        makeCannonBall();
        audio_gun.playInstance();
      }
    }
  };
    private void initAudio() {
    /* Som de tiro com o click do mouse */
    audio_gun = new AudioNode(assetManager, "Sound/Effects/Bang.wav", DataType.Buffer);
    audio_gun.setPositional(false);
    audio_gun.setLooping(false);
    audio_gun.setVolume(2);
    rootNode.attachChild(audio_gun);
    
    audio_nature = new AudioNode(assetManager, "Sounds/sup1.ogg", DataType.Stream);
    audio_nature.setLooping(true);  // Som continuo de fundo
    audio_nature.setPositional(true);
    audio_nature.setVolume(3);
    rootNode.attachChild(audio_nature);
    audio_nature.play();
   }
   public void initMaterials() {
    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
    key2.setGenerateMips(true);
    Texture tex2 = assetManager.loadTexture(key2);
    stone_mat.setTexture("ColorMap", tex2);
  }

   public void makeCannonBall() {
  
    Geometry ball_geo = new Geometry("cannon ball", sphere);
    ball_geo.setMaterial(stone_mat);
    rootNode.attachChild(ball_geo);
    ball_geo.setLocalTranslation(cam.getLocation());
    
    ball_phy = new RigidBodyControl(1f);
    
    ball_geo.addControl(ball_phy);
    bulletAppState.getPhysicsSpace().add(ball_phy);
    
    ball_phy.setLinearVelocity(cam.getDirection().mult(25));
  }

  protected void initCrossHairs() {
    guiNode.detachAllChildren();
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        
    ch.setLocalTranslation( 
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
  }

  private void setUpLight() {
  
    AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(1.3f));
    rootNode.addLight(al);

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White);
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
    rootNode.addLight(dl);
  }

  private void setUpKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
  }

  public void onAction(String binding, boolean isPressed, float tpf) {
    if (binding.equals("Left")) {
      left = isPressed;
    } else if (binding.equals("Right")) {
      right= isPressed;
    } else if (binding.equals("Up")) {
      up = isPressed;
    } else if (binding.equals("Down")) {
      down = isPressed;
    } else if (binding.equals("Jump")) {
 
      if (isPressed) { player.jump(new Vector3f(0,20f,0));}
    }
   if(timer.getTimeInSeconds() > ultimoTempo + 5){
            ultimoTempo = timer.getTimeInSeconds();
            elefante.attachChild(makeElefante());
        }
        
        if(timer.getTimeInSeconds() > dificuldadeT + 5 && elefVelocidade <= 20){
            dificuldadeT = timer.getTimeInSeconds();
            elefVelocidade = elefVelocidade+ 5;
        }
    
  }
    
  protected Spatial makeElefante() {

    Spatial elef = assetManager.loadModel("Models/Elephant/Elephant.mesh.xml");
    elef.scale(0.08f);
    elef.setLocalTranslation(pElef.get(qtdElef%6)); 
    qtdElef++;
    return elef;
  }
  @Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(0.6f);
        camLeft.set(cam.getLeft()).multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        
       for(Spatial s : elefante.getChildren()){
            s.lookAt(cam.getLocation(),  Vector3f.UNIT_Y.normalize());
            s.rotate(0, (float) Math.PI , 0);
            Vector3f dir = s.getLocalTranslation().subtract(cam.getLocation()).normalize().negate();
            System.out.println(cam.getLocation().subtract(s.getLocalTranslation()).getX());
       if(cam.getLocation().subtract(s.getLocalTranslation()).getX() < 5 && cam.getLocation().subtract(s.getLocalTranslation()).getX() > -5
               && cam.getLocation().subtract(s.getLocalTranslation()).getY() < 5 && cam.getLocation().subtract(s.getLocalTranslation()).getY() > -5){
           s.move(dir.x*0*tpf, 0, dir.z*0*tpf);  
        }
       else{
           s.move(dir.x*elefVelocidade*tpf, 0, dir.z*elefVelocidade*tpf);
        }  
        }
        cam.setLocation(player.getPhysicsLocation());
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }
 }
