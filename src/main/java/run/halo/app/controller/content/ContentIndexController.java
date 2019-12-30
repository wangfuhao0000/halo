package run.halo.app.controller.content;

import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog index page controller
 *
 * @author ryanwang
 * @date : 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping
public class ContentIndexController {

    private final PostService postService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public ContentIndexController(PostService postService,
                                  OptionService optionService,
                                  ThemeService themeService) {
        this.postService = postService;
        this.optionService = optionService;
        this.themeService = themeService;
    }


    /**
     * Render blog index
     *
     * @param model model
     * @return template path: themes/{theme}/index.ftl
     */
    @GetMapping
    public String index(Model model) {
        return this.index(model, 1);
    }

    /**
     * Render blog index
     *
     * @param model model
     * @param page  current page number
     * @return template path: themes/{theme}/index.ftl
     */
    @GetMapping(value = "page/{page}")
    public String index(Model model,
                        @PathVariable(value = "page") Integer page) {
        // 所以就是OptionService是访问Properties的入口类，调用OptionService的方法都是获取Properties的相关值
        String indexSort = optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT).toString();  // 得到排序的规则
        int pageSize = optionService.getPostPageSize();     // 其实也是获取PostProperties.INDEX_PAGE_SIZE
        // 构造一个和分页相关的变量，自定义要查询的分页属性：页数、一页的个数、排序规则等
        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, pageSize, Sort.by(DESC, "topPriority").and(Sort.by(DESC, indexSort)));

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);  // Post是继承了一个实体，在这里作为一个页，它有很多的属性
        Page<PostListVO> posts = postService.convertToListVo(postPage);  // 将得到的所有页转为对象

        int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);  // 彩虹分页算法，一次最多显示三页

        model.addAttribute("is_index", true);
        model.addAttribute("posts", posts);     // 分页的内容
        model.addAttribute("rainbow", rainbow);     // 分页的数字
        return themeService.render("index");    // 因为把数据都放在了Model里了，渲染时的前端会直接拿出来用
    }
}
