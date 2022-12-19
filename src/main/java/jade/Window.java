package jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int width,height;
    private String title;
    private long glfwWindow;
    private ImGuiLayer imGuiLayer;
    public float r,g,b,a;
    private static boolean fade=false;

    private static Scene currentScene;

    private static Window window=null;

    private Window(){
        this.width=1920;
        this.height=1080;
        this.title="Mario";
        this.r=1;
        this.g=1;
        this.b=1;
        this.a=1;

    }
    public static void changeScene(int newScene)
    {
        switch (newScene)
        {
            case 0:
                currentScene=new LevelEditorScene();
                break;
            case 1:
                currentScene=new LevelScene();
                break;
            default:
                assert false:"Unknown scene '"+newScene+"'";
                break;
        }
        currentScene.load();
        currentScene.init();
        currentScene.start();

    }

    public static Window get()
    {
        if (Window.window==null)
        {
            Window.window=new Window();
        }

        return Window.window;
    }

    public static Scene getScene()
    {
        return get().currentScene;
    }


    public void run()
    {
        System.out.println("hello lwjgl"+ Version.getVersion());

        init();
        loop();

        //free memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        //free error callbacks
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init()
    {
        //error callback
        GLFWErrorCallback.createPrint(System.err).set();

        //initialize glfw
        if (!glfwInit())
        {
            throw new IllegalStateException("cant initialize glfw.");
        }

        //configure glfw
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE,GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED,GLFW_TRUE);

        //create window
        glfwWindow=glfwCreateWindow(this.width,this.height,this.title,NULL,NULL);

        if (glfwWindow==NULL)
        {
            throw new IllegalStateException("failed to create glfw window.");
        }

        glfwSetCursorPosCallback(glfwWindow,MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow,MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow,MouseListener::mouseScrollCallback);

        glfwSetKeyCallback(glfwWindow,KeyListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow,(w,newWidth,newHeight)->{
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        //openGL context current
        glfwMakeContextCurrent(glfwWindow);

        //v-sync
        glfwSwapInterval(1);

        glfwShowWindow(glfwWindow);

        //binding things
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE,GL_ONE_MINUS_SRC_ALPHA);
        this.imGuiLayer=new ImGuiLayer(glfwWindow);
        this.imGuiLayer.initImGui();
        Window.changeScene(0);

    }
    public void loop()
    {
        float beginTime= (float)glfwGetTime();
        float endTime;
        float dt=-1.0f;


        while(!glfwWindowShouldClose(glfwWindow))
        {
            //pool events
            glfwPollEvents();

            glClearColor(r,g,b,a);
            glClear(GL_COLOR_BUFFER_BIT);
            if(dt>=0)
            {
                currentScene.update(dt);
            }
            this.imGuiLayer.update(dt,currentScene);
            glfwSwapBuffers(glfwWindow);

            endTime=(float)glfwGetTime();
            dt=endTime-beginTime;
            beginTime=endTime;
        }
        currentScene.saveExit();
    }
    public static int getWidth()
    {
        return get().width;
    }

    public static int getHeight()
    {
        return get().height;
    }

    public static void setWidth(int newWidth)
    {
        get().width=newWidth;
    }
    public static void setHeight(int newHeight)
    {
        get().height=newHeight;
    }


}
