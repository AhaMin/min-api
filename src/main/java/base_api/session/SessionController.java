package base_api.session;

import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import base_core.session.dao.MessageDAO;
import base_core.session.dao.SessionDAO;
import base_core.session.model.Message;
import base_core.session.model.Session;
import base_core.user.dao.UserDAO;
import base_core.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * created by ewang on 2018/4/19.
 */
@Controller
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SessionDAO sessionDAO;

    @Autowired
    private MessageDAO messageDAO;

    @RequestMapping("/find")
    public ResponseWrapper findFriends(@RequestParam("userId") long currentUserId) {
        User currentUser = userDAO.getById(currentUserId);
        if (null == currentUser) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }

        List<User> userList = new ArrayList<>();
        Map<Long, String> messageMap = new HashMap<>();
        Map<Long, Session> sessionMap = new HashMap<>();
        sessionDAO.findLatestSessionByUser(currentUserId, 20).stream().forEach(s -> {
            long toUserId = s.getToUserId();
            User user = userDAO.getById(toUserId);
            Message message = messageDAO.getLatestBySession(s.getId());
            if (message != null && user != null) {
                userList.add(user);
                sessionMap.put(user.getId(), s);
                messageMap.put(toUserId, message.getContent());
            }
        });

        return new ResponseWrapper()
                .addObject("userList", userList)
                .addObject("messageMap", messageMap)
                .addObject("sessionMap", sessionMap);

    }

    @RequestMapping("/message/find")
    public ResponseWrapper getMessage(@RequestParam("userId") long userId,
                                      @RequestParam("toUserId") long toUserId) {
        Session session = findOrCreateSession(userId, toUserId);
        List<Message> messages = messageDAO.findBySession(session.getId());
        int oldUnread = session.getUnread();
        if (oldUnread != 0) {
            //清除未读消息
            sessionDAO.updateUnread(session.getId(), oldUnread, 0);
        }
        return new ResponseWrapper().addObject("messageList", messages);
    }

    @RequestMapping("/message/send")
    public ResponseWrapper sendMessage(@RequestParam("userId") long userId,
                                       @RequestParam("toUserId") long toUserId,
                                       @RequestParam("content") String content) {
        Session session = findOrCreateSession(userId, toUserId);
        Session toSession = findOrCreateSession(toUserId, userId);

        long messageId = messageDAO.insert(userId, toUserId, session.getId(), content);
        Message message = messageDAO.getById(messageId);
        sessionDAO.updateTime(session.getId(), message.getCreateTime());

        if (!Objects.equals(userId, toUserId)) {
            long toSessionId = toSession.getId();
            int oldUnread = toSession.getUnread();
            messageDAO.insert(userId, toUserId, toSessionId, content);
            sessionDAO.updateTime(toSessionId, message.getCreateTime());
            sessionDAO.updateUnread(toSessionId, oldUnread, oldUnread + 1);
        }
        return new ResponseWrapper().addObject("message", message);
    }

    @RequestMapping("/clear/unread")
    public ResponseWrapper clearUnread(@RequestParam("userId") long userId,
                                       @RequestParam("toUserId") long toUserId) {
        Session session = sessionDAO.getByUserAndToUser(userId, toUserId);
        if (session != null) {
            int result = sessionDAO.updateUnread(session.getId(), session.getUnread(), 0);
            if (result > 0) {
                return new ResponseWrapper();
            }
        }
        return new ResponseWrapper(ResponseStatus.ServerError, "清理未读消息失败");
    }

    Session findOrCreateSession(long userId, long toUserId) {
        Session session = sessionDAO.getByUserAndToUser(userId, toUserId);
        if (session == null) {
            long sessionId = sessionDAO.insert(userId, toUserId);
            session = sessionDAO.getById(sessionId);
        }
        return session;
    }
}
