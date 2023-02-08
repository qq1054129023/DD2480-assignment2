package group22;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import group22.service.GitService;
import group22.utils.JGitUtils;
import org.apache.maven.shared.invoker.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.eclipse.jgit.api.Git;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import group22.utils.CloneRepository;
import group22.utils.Helpers;


/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class CIServer extends AbstractHandler
{
    public static final String localRepoPath = "./repo";

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        //System.out.println(target);

        JSONObject jsonObject = new JSONObject();
        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository

        try{
            jsonObject = Helpers.convertBody(request);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        if(!Helpers.judgeDirExist(localRepoPath)) {
            String cloneUrl = Helpers.getCloneUrl(jsonObject);
            CloneRepository.cloneRepository(cloneUrl,  localRepoPath);
        }

        Git git = JGitUtils.openRpo(localRepoPath);

        String branchName = Helpers.getBranchName(jsonObject);

        GitService.pullRepo(git, branchName);

        // 2nd compile the code

        // test
        boolean testRsl = GitService.testBranch(git, branchName);

        if(!testRsl){
            response.setStatus(400);
            response.getWriter().println("test failed");
        }
        else {
            response.getWriter().println("test passed");
        }
    }
 
    // used to start the CI server in command line.
    public static void main(String[] args) throws Exception
    {

        Server server = new Server(8080);
        server.setHandler(new CIServer());
        server.start();
        server.join();

    }
}
